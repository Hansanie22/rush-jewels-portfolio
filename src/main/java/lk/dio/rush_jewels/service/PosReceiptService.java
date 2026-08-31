package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.PosReceiptDTO;
import lk.dio.rush_jewels.model.OrderItems;
import lk.dio.rush_jewels.model.Orders;
import lk.dio.rush_jewels.model.Payment;
import lk.dio.rush_jewels.repository.OrderItemsRepository;
import lk.dio.rush_jewels.repository.OrdersRepository;
import lk.dio.rush_jewels.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class PosReceiptService {

    private final OrdersRepository ordersRepo;
    private final OrderItemsRepository orderItemsRepo;
    private final PaymentRepository paymentRepo;

    public PosReceiptService(OrdersRepository ordersRepo, OrderItemsRepository orderItemsRepo, PaymentRepository paymentRepo) {
        this.ordersRepo = ordersRepo;
        this.orderItemsRepo = orderItemsRepo;
        this.paymentRepo = paymentRepo;
    }

    public PosReceiptDTO generateReceipt(String orderId) {
        Orders order = ordersRepo.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        Payment payment = paymentRepo.findByOrders(order).orElseThrow(() -> new RuntimeException("Payment not found"));
        List<OrderItems> items = orderItemsRepo.findByOrders(order);

        PosReceiptDTO receipt = new PosReceiptDTO();
        receipt.setOrderId(order.getId());
        receipt.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getOrderedAt()));
        receipt.setCashierName(order.getUser().getFname()); // Assuming cashier logged the walk-in, or we can use security context
        receipt.setCustomerMobile(order.getUser().getMobile());

        List<PosReceiptDTO.PosReceiptItemDTO> itemDTOs = new ArrayList<>();

        for (OrderItems oi : items) {
            PosReceiptDTO.PosReceiptItemDTO dto = new PosReceiptDTO.PosReceiptItemDTO();
            dto.setItemName(oi.getProductVariance().getProduct().getTitle());
            dto.setQty(oi.getQty());
            dto.setUnitPrice(oi.getProductVariance().getPrice());
            dto.setTotalPrice(oi.getProductVariance().getPrice() * oi.getQty());
            itemDTOs.add(dto);
        }
        receipt.setItems(itemDTOs);

        receipt.setSubTotal(payment.getSubTotal());
        receipt.setDiscount(payment.getDiscount());
        receipt.setFinalTotal(payment.getFinalTotal());
        receipt.setPaymentMethod(payment.getPaymentsMethod().getMethod());

        // As most products in RUSH JEWELS are Gold/Silver Plated, always include the warranty.
        receipt.setWarrantyInfo("Includes a 6-Month Color Guarantee. Keep this receipt for warranty claims. Thank you for shopping with RUSH JEWELS!");

        return receipt;
    }
}
