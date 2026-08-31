package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.PaymentMethodDTO;
import lk.dio.rush_jewels.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    public List<PaymentMethodDTO> getAllPaymentMethods() {
        return paymentMethodRepository.findByIsActiveTrueOrderByIdAsc()
                .stream()
                .map(PaymentMethodDTO::new)
                .collect(Collectors.toList());
    }
}