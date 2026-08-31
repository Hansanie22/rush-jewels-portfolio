package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.repository.OrdersRepository;
import lk.dio.rush_jewels.repository.OrderStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class BankSlipService {

    private final OrdersRepository ordersRepo;
    private final OrderStatusRepository orderStatusRepo;
    private final CloudinaryService cloudinaryService;

    public BankSlipService(OrdersRepository ordersRepo, OrderStatusRepository orderStatusRepo, CloudinaryService cloudinaryService) {
        this.ordersRepo = ordersRepo;
        this.orderStatusRepo = orderStatusRepo;
        this.cloudinaryService = cloudinaryService;
    }

    public String uploadSlipForOrder(String orderId, MultipartFile file) throws IOException {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!"Pending Slip Verification".equalsIgnoreCase(order.getOrderStatus().getOrderStatus())) {
            throw new RuntimeException("Order is not awaiting slip verification.");
        }

        // Upload to Cloudinary
        String slipUrl = cloudinaryService.uploadImage(file);

        // Update Order
        order.setSlipUrl(slipUrl);
        ordersRepo.save(order);

        return slipUrl;
    }

    public void verifySlip(String orderId, String status) {
        Orders order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if ("APPROVED".equalsIgnoreCase(status)) {
            order.setOrderStatus(orderStatusRepo.findByOrderStatus("Processing").orElseThrow());
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            order.setOrderStatus(orderStatusRepo.findByOrderStatus("Cancelled").orElseThrow());
            // Optionally, add a note why it was rejected
        } else {
            throw new RuntimeException("Invalid verification status.");
        }

        ordersRepo.save(order);
    }
}
