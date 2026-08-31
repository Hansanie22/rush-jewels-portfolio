package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ShippingDTO;
import lk.dio.rush_jewels.repository.ShippingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;

    public ShippingService(ShippingRepository shippingRepository) {
        this.shippingRepository = shippingRepository;
    }

    public List<ShippingDTO> getAllShippingMethods() {
        return List.of(
            new ShippingDTO(1, "Standard Shipping (Flat Rate)", 400.00, "Island-wide delivery")
        );
    }
}