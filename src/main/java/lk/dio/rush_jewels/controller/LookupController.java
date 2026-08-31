package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.OrderStatus;
import lk.dio.rush_jewels.model.PaymentStatus;
import lk.dio.rush_jewels.repository.OrderStatusRepository;
import lk.dio.rush_jewels.repository.PaymentStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
    @RequestMapping("/api/admin/lookups")
    public class LookupController {

        @Autowired
        private PaymentStatusRepository paymentStatusRepository;

        @Autowired
        private OrderStatusRepository orderStatusRepository;

        @GetMapping("/payment-status")
        public List<PaymentStatus> getPaymentStatuses() {
            return paymentStatusRepository.findAll();
        }

        @GetMapping("/order-status")
        public List<OrderStatus> getOrderStatuses() {
            return orderStatusRepository.findAll();
        }
    }


