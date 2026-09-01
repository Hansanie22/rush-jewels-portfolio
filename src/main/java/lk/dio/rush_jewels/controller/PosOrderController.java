package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.PosCheckoutRequestDTO;
import lk.dio.rush_jewels.service.PosOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.StockRepository;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Stock;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.UserRepository;
import lk.dio.rush_jewels.dto.PosProductDTO;
import jakarta.servlet.http.HttpServletRequest;
import lk.dio.rush_jewels.repository.StatusRepository;
import lk.dio.rush_jewels.model.Status;
import java.util.Date;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lk.dio.rush_jewels.repository.PosShiftRepository;
import lk.dio.rush_jewels.model.PosShift;
import lk.dio.rush_jewels.dto.PosShiftRequestDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pos")
public class PosOrderController {

    private final PosOrderService posOrderService;
    private final ProductVarianceRepository productVarianceRepo;
    private final StockRepository stockRepo;
    private final UserRepository userRepo;
    private final StatusRepository statusRepo;
    private final lk.dio.rush_jewels.repository.CollectionRepository collectionRepo;
    private final PosShiftRepository posShiftRepo;

    public PosOrderController(PosOrderService posOrderService, ProductVarianceRepository productVarianceRepo, StockRepository stockRepo, UserRepository userRepo, StatusRepository statusRepo, lk.dio.rush_jewels.repository.CollectionRepository collectionRepo, PosShiftRepository posShiftRepo) {
        this.posOrderService = posOrderService;
        this.productVarianceRepo = productVarianceRepo;
        this.stockRepo = stockRepo;
        this.userRepo = userRepo;
        this.statusRepo = statusRepo;
        this.collectionRepo = collectionRepo;
        this.posShiftRepo = posShiftRepo;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody PosCheckoutRequestDTO request) {
        try {
            Object result = posOrderService.processPosOrder(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/end-shift")
    public ResponseEntity<?> endShift(@RequestBody PosShiftRequestDTO request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status", false, "message", "Unauthorized"));
        }

        try {
            PosShift shift = new PosShift();
            shift.setCashier(user);
            shift.setStartTime(request.getStartTime());
            shift.setEndTime(request.getEndTime());
            shift.setTotalSales(request.getTotalSales());
            shift.setCashSales(request.getCashSales());
            shift.setCardSales(request.getCardSales());
            shift.setReturnAmount(request.getReturnAmount());
            shift.setExpectedCash(request.getExpectedCash());
            shift.setActualCash(request.getActualCash());
            shift.setPettyCashUsed(request.getPettyCashUsed());
            shift.setDifferenceReason(request.getDifferenceReason());

            posShiftRepo.save(shift);

            return ResponseEntity.ok(Map.of("status", true, "message", "Shift ended successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("status", false, "message", "Error ending shift: " + e.getMessage()));
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<PosProductDTO>> getAllPosProducts(HttpServletRequest request) {
        List<ProductVariance> variances = productVarianceRepo.findAll();
        List<PosProductDTO> dtoList = new java.util.ArrayList<>(mapToPosProducts(variances, request));
        
        // Add active collections
        List<lk.dio.rush_jewels.model.Collection> collections = collectionRepo.findByStatus_Id(1);
        dtoList.addAll(mapCollectionsToPosProducts(collections, request));
        
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/customer")
    public ResponseEntity<?> getCustomerByMobile(@RequestParam("mobile") String mobile) {
        return userRepo.findFirstByMobile(mobile)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customer")
    public ResponseEntity<?> createCustomer(@RequestBody User userRequest) {
        try {
            if (userRequest.getMobile() == null || userRequest.getMobile().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mobile number is required");
            }
            if (userRequest.getLname() == null || userRequest.getLname().trim().isEmpty()) {
                userRequest.setLname("");
            }
            if (userRequest.getFname() == null || userRequest.getFname().trim().isEmpty()) {
                userRequest.setFname("New");
            }
            if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty()) {
                userRequest.setEmail(userRequest.getMobile() + "@rushjewels.local");
            }

            // Check if user already exists
            if (userRepo.findFirstByMobile(userRequest.getMobile()).isPresent() || 
                userRepo.findByEmail(userRequest.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Customer with this mobile or email already exists");
            }
            
            userRequest.setPassword("pos_customer_placeholder"); // Since @JsonIgnore might drop it
            userRequest.setLoginProvider("LOCAL");
            userRequest.setType("USER");
            userRequest.setSubscribed(false);
            
            Status activeStatus = statusRepo.findById(1).orElseThrow(() -> new RuntimeException("Status 1 not found"));
            userRequest.setStatus(activeStatus);
            userRequest.setCreatedAt(new Date());
            
            User savedUser = userRepo.save(userRequest);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Database Error: " + e.getMessage());
        }
    }

    @GetMapping("/products/search")
    public ResponseEntity<List<PosProductDTO>> searchPosProducts(@RequestParam("q") String query, HttpServletRequest request) {
        // Find by barcode (variant ID) if numeric
        try {
            int variantId = Integer.parseInt(query.trim());
            ProductVariance pv = productVarianceRepo.findById(variantId).orElse(null);
            if (pv != null) {
                return ResponseEntity.ok(mapToPosProducts(List.of(pv), request));
            }
        } catch (NumberFormatException e) {
            // Not a number, fallback to text search
        }
        
        List<ProductVariance> variances = productVarianceRepo.searchActiveProducts(query);
        List<PosProductDTO> dtoList = new java.util.ArrayList<>(mapToPosProducts(variances, request));
        
        // Search collections
        List<lk.dio.rush_jewels.model.Collection> collections = collectionRepo.searchActiveCollections(query);
        dtoList.addAll(mapCollectionsToPosProducts(collections, request));
        
        return ResponseEntity.ok(dtoList);
    }

    private List<PosProductDTO> mapToPosProducts(List<ProductVariance> variances, HttpServletRequest request) {
        // Build base URL — respect X-Forwarded headers set by Nginx reverse proxy
        String scheme = request.getHeader("X-Forwarded-Proto") != null ? request.getHeader("X-Forwarded-Proto") : request.getScheme();
        String host = request.getHeader("X-Forwarded-Host") != null ? request.getHeader("X-Forwarded-Host") : (request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort()));
        String baseUrl = scheme + "://" + host + request.getContextPath();

        return variances.stream().map(pv -> {
            int stock = stockRepo.findFirstByProductVariance(pv).map(Stock::getQty).orElse(0);
            String name = pv.getProduct().getName();
            
            // Build clear subtext for variations
            StringBuilder subtext = new StringBuilder();
            if (pv.getSize() != null) subtext.append("Size: ").append(pv.getSize().getSize()).append("  ");
            if (pv.getColor() != null) subtext.append("Metal: ").append(pv.getColor().getColor()).append("  ");
            if (pv.getGemstone() != null) subtext.append("Gem: ").append(pv.getGemstone().getGemStone()).append("  ");

            String imagePath = pv.getProduct().getImage1();
            String imageUrl = null;
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    imageUrl = imagePath;
                } else {
                    // Avoid double slash: imagePath may start with '/'
                    String cleanPath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                    imageUrl = baseUrl + cleanPath.replace("\\", "/");
                }
            }

            String category = (pv.getProduct().getCategory() != null) ? pv.getProduct().getCategory().getCategory() : "Other";

            return new PosProductDTO("P-" + pv.getId(), "PRODUCT", name, subtext.toString().trim(), pv.getPrice(), stock, String.valueOf(pv.getId()), imageUrl, category);
        }).collect(Collectors.toList());
    }

    private List<PosProductDTO> mapCollectionsToPosProducts(List<lk.dio.rush_jewels.model.Collection> collections, HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto") != null ? request.getHeader("X-Forwarded-Proto") : request.getScheme();
        String host = request.getHeader("X-Forwarded-Host") != null ? request.getHeader("X-Forwarded-Host") : (request.getServerName() + (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort()));
        String baseUrl = scheme + "://" + host + request.getContextPath();

        return collections.stream().map(c -> {
            String name = c.getName() + " (Collection)";
            String subtext = "Bundle / Collection";
            
            String imagePath = c.getImage1();
            String imageUrl = null;
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                    imageUrl = imagePath;
                } else {
                    // Avoid double slash: imagePath may start with '/'
                    String cleanPath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                    imageUrl = baseUrl + cleanPath.replace("\\", "/");
                }
            }

            return new PosProductDTO("C-" + c.getId(), "COLLECTION", name, subtext, c.getPrice(), c.getStockLimit(), "C-" + c.getId(), imageUrl, "Collection");
        }).collect(Collectors.toList());
    }
}
