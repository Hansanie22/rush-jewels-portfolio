package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.ShipmentDTO;
import lk.dio.rush_jewels.dto.ShipmentDetailDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminLogisticsService {

    private final ShipmentsRepository shipmentsRepository;
    private final OrdersRepository ordersRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    // Repositories for Return Logic
    private final ReturnRepository returnRepository;
    private final ReturnItemsRepository returnItemsRepository;
    private final ReturnTypeRepository returnTypeRepository;
    private final OrderItemsRepository orderItemsRepository;

    public AdminLogisticsService(ShipmentsRepository shipmentsRepository,
                                 OrdersRepository ordersRepository,
                                 CourierCompanyRepository courierRepository,
                                 AdminAuditLogRepository auditLogRepository,
                                 ObjectMapper objectMapper,
                                 ReturnRepository returnRepository,
                                 ReturnItemsRepository returnItemsRepository,
                                 ReturnTypeRepository returnTypeRepository,
                                 OrderItemsRepository orderItemsRepository) {
        this.shipmentsRepository = shipmentsRepository;
        this.ordersRepository = ordersRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.returnRepository = returnRepository;
        this.returnItemsRepository = returnItemsRepository;
        this.returnTypeRepository = returnTypeRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    // 1. Get All Shipments
    public List<ShipmentDTO> getAllShipments() {
        checkAndAutoUpdateStatuses();
        return shipmentsRepository.findAllByOrderByShippedDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. Get Details
    public ShipmentDetailDTO getShipmentDetails(int id) {
        Shipments s = shipmentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        Orders o = s.getOrder();
        User u = o.getUser();
        DeliveryAddress addr = o.getDeliveryAddress();
        double total = 0.0;

        return new ShipmentDetailDTO(
                s.getTrackingNumber(),
                s.getStatus().toString(),
                s.getShippedDate(),
                s.getEstimatedDelivery(),
                o.getId(),
                o.getOrderedAt(),
                total,
                u.getFname() + " " + u.getLname(),
                u.getEmail(),
                addr.getLine1(),
                addr.getLine2(),
                addr.getCity() != null ? addr.getCity().getCity() : addr.getCityText(),
                addr.getContactNo()
        );
    }

    // 3. Save Shipment (Create / Update)
    public Shipments saveShipment(ShipmentDTO dto) {
        Shipments shipment;
        String action = "CREATE";
        String oldValue = null;

        if (dto.getId() > 0) {
            action = "UPDATE";
            shipment = shipmentsRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Shipment not found"));

            oldValue = convertToJson(shipment);

            shipment.setTrackingNumber(dto.getTrackingNumber());
            shipment.setShippedDate(dto.getShippedDate());

            if (dto.getStatus() != null) {
                ShipmentStatus newStatus = ShipmentStatus.valueOf(dto.getStatus());

                // ✅ LOGIC: If Status Changed to RETURNED
                // Check if it WAS NOT returned before to prevent re-triggering
                if (newStatus == ShipmentStatus.RETURNED && shipment.getStatus() != ShipmentStatus.RETURNED) {
                    processReturnedShipment(shipment.getOrder());
                }

                shipment.setStatus(newStatus);
            }

            shipment.setEstimatedDelivery(calculateEstimatedDate(dto.getShippedDate()));

        } else {
            shipment = new Shipments();
            Orders order = ordersRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order ID " + dto.getOrderId() + " not found"));
            shipment.setOrder(order);
            shipment.setTrackingNumber(dto.getTrackingNumber());
            shipment.setShippedDate(dto.getShippedDate());
            shipment.setEstimatedDelivery(calculateEstimatedDate(dto.getShippedDate()));
            shipment.setStatus(ShipmentStatus.SHIPPED);
        }

        Shipments saved = shipmentsRepository.save(shipment);
        logAction(action, "shipments", String.valueOf(saved.getId()), oldValue, saved);
        return saved;
    }

    // ✅ HELPER: Process Returned Shipment with VALIDATION
    private void processReturnedShipment(Orders order) {
        // 1. VALIDATION: Check if a return record already exists for this order
        if (returnRepository.existsByOrders(order)) {
            // Return already exists, skip creation to prevent duplicates
            return;
        }

        // 2. Get Return Type ID = 1 (System / Auto Return)
        ReturnType type = returnTypeRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Return Type ID 1 not found"));

        // 3. Create Return Record
        Return ret = new Return();

        String prefix = "RTN";
// Format date as YYYY-MM-DD
        String date = java.time.LocalDate.now().toString();  // 2025-11-30
        String sequence = String.format("%03d", 1); // 001
        ret.setId(prefix + "-" + date + "-" + sequence);
        ret.setOrders(order);
        ret.setReturnType(type);
        ret.setRequestDate(LocalDateTime.now());
        ret.setApprovedDate(LocalDateTime.now());
        ret.setStatus(ReturnStatus.APPROVED);
        ret.setReturnReason("Delivery Failed - Customer not picked (Auto Generated)");

        Return savedReturn = returnRepository.save(ret);

        // 4. Move Order Items to Return Items
        List<OrderItems> items = orderItemsRepository.findByOrders(order);

        for (OrderItems item : items) {
            ReturnItems ri = new ReturnItems();
            ri.setReturns(savedReturn);
            ri.setOrderItems(item);
            ri.setQty(item.getQty());
            returnItemsRepository.save(ri);
        }
    }

    // Auto Update Logic
    private void checkAndAutoUpdateStatuses() {
        List<Shipments> activeShipments = shipmentsRepository.findAll();
        Date now = new Date();

        for (Shipments s : activeShipments) {
            if (s.getShippedDate() == null || s.getStatus() == ShipmentStatus.RETURNED) continue;

            long diffInMillies = Math.abs(now.getTime() - s.getShippedDate().getTime());
            long diffDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            boolean changed = false;
            String oldVal = convertToJson(s);

            if (diffDays >= 1 && s.getStatus() == ShipmentStatus.SHIPPED) {
                s.setStatus(ShipmentStatus.IN_TRANSIT);
                changed = true;
            }
            else if (diffDays >= 2 && s.getStatus() == ShipmentStatus.IN_TRANSIT) {
                s.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
                changed = true;
            }

            if (changed) {
                Shipments updated = shipmentsRepository.save(s);
                logAction("AUTO_STATUS_UPDATE", "shipments", String.valueOf(updated.getId()), oldVal, updated);
            }
        }
    }

    // Helpers
    private Date calculateEstimatedDate(Date shippedDate) {
        if (shippedDate == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(shippedDate);
        c.add(Calendar.DATE, 3);
        return c.getTime();
    }

    private ShipmentDTO convertToDTO(Shipments s) {
        String city = "Unknown";
        if (s.getOrder().getDeliveryAddress() != null) {
            DeliveryAddress da = s.getOrder().getDeliveryAddress();
            city = (da.getCity() != null) ? da.getCity().getCity() : da.getCityText();
        }

        return new ShipmentDTO(
                s.getId(),
                s.getTrackingNumber(),
                s.getOrder().getId(),
                s.getShippedDate(),
                s.getStatus().toString(),
                "Fardar",
                city
        );
    }

    private void logAction(String action, String table, String recordId, String oldValue, Object newValueObj) {
        try {
            String newValue = convertToJson(newValueObj);
            AdminAuditLog log = new AdminAuditLog(
                    action,
                    table,
                    recordId,
                    oldValue,
                    newValue,
                    LocalDateTime.now()
            );
            auditLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Audit Log Failed: " + e.getMessage());
        }
    }

    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(sanitizeForAudit(object));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Object sanitizeForAudit(Object obj) {
        if (obj instanceof Shipments) {
            Shipments s = (Shipments) obj;
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("tracking", s.getTrackingNumber());
            map.put("status", s.getStatus());
            map.put("orderId", s.getOrder().getId());
            return map;
        }
        return obj;
    }
}