package lk.dio.rush_jewels.controller;

import jakarta.servlet.http.HttpSession;
import lk.dio.rush_jewels.dto.CheckoutDetailsDTO;
import lk.dio.rush_jewels.dto.DeliveryAddressDTO;
import lk.dio.rush_jewels.dto.DeliveryAddressRequestDTO; // <-- Import the new Request DTO
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.service.DeliveryAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class DeliveryAddressController {

    private final DeliveryAddressService addressService;
    private static final String USER_SESSION_KEY = "user";

    public DeliveryAddressController(DeliveryAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<?> getAddresses(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "User not authenticated"));
        }

        List<DeliveryAddressDTO> addresses = addressService.getAddressesByUser(user);
        return ResponseEntity.ok(Map.of("status", true, "addresses", addresses));
    }

    @PostMapping
    public ResponseEntity<?> saveAddress(HttpSession session,
                                         @RequestBody DeliveryAddressRequestDTO requestDTO) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "User not authenticated"));
        }

        try {
            // --- Pass the user and the entire DTO to the service ---
            DeliveryAddressDTO saved = addressService.saveOrUpdateAddress(user, requestDTO);

            return ResponseEntity.ok(Map.of(
                    "status", true,
                    "message", "Address saved successfully!",
                    "address", saved
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", "Failed to save address: " + e.getMessage()));
        }
    }

    @GetMapping("/checkout-details")
    public ResponseEntity<?> getCheckoutDetails(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            // It's not an error to be a guest, just return an "empty" status
            // The frontend will handle this gracefully
            return ResponseEntity.status(401)
                    .body(Map.of("status", false, "message", "User not authenticated"));
        }

        try {
            CheckoutDetailsDTO details = addressService.getCheckoutDetails(user);
            return ResponseEntity.ok(Map.of("status", true, "data", details));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", false, "message", "Error fetching details: " + e.getMessage()));
        }
    }
}