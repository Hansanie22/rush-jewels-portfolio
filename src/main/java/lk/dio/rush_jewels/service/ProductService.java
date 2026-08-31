package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.OrderItemsRepository;
import lk.dio.rush_jewels.repository.StockRepository;
import lk.dio.rush_jewels.repository.CategoryRepository;
import lk.dio.rush_jewels.repository.ReviewRepository;
import lk.dio.rush_jewels.dto.ProductDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductVarianceRepository productVarianceRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;

    // ✅ NOTE: Image Base Path removed.
    // We now use Cloudinary URLs stored directly in the database.

    private static final int COMPLETED_ORDER_STATUS_ID = 3;
    private static final int ACTIVE_STATUS_ID = 1;

    // 🏭 Warehouse Configuration
    private static final int WAREHOUSE_ID_MAIN = 1;

    // 📦 Stock Status Configuration
    private static final int OUT_OF_STOCK_STATUS_ID = 3;

    public ProductService(ProductVarianceRepository productVarianceRepository,
                          OrderItemsRepository orderItemsRepository,
                          StockRepository stockRepository,
                          CategoryRepository categoryRepository,
                          ReviewRepository reviewRepository) {
        this.productVarianceRepository = productVarianceRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
    }

    // --- Navbar: Load Active Categories ---
    public List<String> getActiveCategories() {
        return categoryRepository.findByStatusId(ACTIVE_STATUS_ID).stream()
                .map(Category::getCategory)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> loadFilteredProducts(
            String[] categoryFilters,
            String[] gemstoneFilters,
            String[] metalFilters,
            Double maxPrice,
            String contextPath) {

        List<ProductVariance> variances = productVarianceRepository.findByProduct_Status_Id(ACTIVE_STATUS_ID);
        Map<Integer, Integer> productSales = calculateProductSales(variances);
        Date now = new Date();
        Double finalMaxPrice = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;

        return variances.stream()
                .map(pv -> mapVarianceToDTO(pv, productSales, now))
                .filter(dto -> filterDTO(dto, categoryFilters, gemstoneFilters, metalFilters, finalMaxPrice))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> calculateProductSales(List<ProductVariance> variances) {
        Map<Integer, Integer> sales = new HashMap<>();
        for (ProductVariance pv : variances) {
            Integer totalSold = orderItemsRepository.sumQtyByProductVarianceAndOrderStatusId(pv, COMPLETED_ORDER_STATUS_ID);
            sales.put(pv.getId(), totalSold != null ? totalSold : 0);
        }
        return sales;
    }

    private ProductDTO mapVarianceToDTO(ProductVariance pv, Map<Integer, Integer> productSales, Date now) {
        Product product = pv.getProduct();
        ProductDTO dto = new ProductDTO();

        // --- Calculate Available Stock ---
        List<Stock> stocks = stockRepository.findByProductVariance(pv);
        int currentStockQty = 0;

        for (Stock st : stocks) {
            if (st.getWarehouse() != null && st.getWarehouse().getId() == WAREHOUSE_ID_MAIN) {
                int statusId = st.getStockStatus().getId();
                if (statusId != OUT_OF_STOCK_STATUS_ID) {
                    currentStockQty += st.getQty();
                }
            }
        }

        String stockStatus;
        if (currentStockQty > 0) {
            if (currentStockQty <= 5) {
                stockStatus = "Low Stock";
            } else {
                stockStatus = "In Stock";
            }
        } else {
            stockStatus = "Out Of Stock";
            currentStockQty = 0;
        }

        // --- Populate DTO ---
        double price = pv.getPrice() != null ? pv.getPrice() : pv.getRegularPrice();

        dto.setProductId(product.getId());
        dto.setVarianceId(pv.getId());
        dto.setName(product.getName());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setCategory(product.getCategory() != null ? product.getCategory().getCategory() : "");
        dto.setRegularPrice(pv.getRegularPrice());
        dto.setPrice(price);
        dto.setDiscountPercentage(pv.getDiscountPercentage() != null ? pv.getDiscountPercentage() : 0.0);
        dto.setSize(pv.getSize() != null ? pv.getSize().getSize() : "");
        dto.setColor(pv.getColor() != null ? pv.getColor().getColor() : "");
        dto.setGemstone(pv.getGemstone() != null ? pv.getGemstone().getGemStone() : "");

        dto.setStockLimit(currentStockQty);
        dto.setStockStatus(stockStatus);
        dto.setCurrentStockQty(currentStockQty);

        // --- FETCH REVIEWS (Product Level) ---
        Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
        int reviewCount = reviewRepository.getReviewCountByProductId(product.getId());

        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        dto.setReviewCount(reviewCount);

        // --- IMAGE URL (UPDATED) ---
        // ✅ OLD: String imagePath = "/uploads" + imageBasePath + "/" + product.getId() + "/image1.png";
        // ✅ NEW: කෙලින්ම Product එකේ Cloudinary URL එක ගන්නවා
        dto.setImage(product.getImage1());

        dto.setTags(determineTags(pv, productSales.getOrDefault(pv.getId(), 0), now, currentStockQty));

        return dto;
    }

    private List<String> determineTags(ProductVariance pv, int soldQty, Date now, int currentStockQty) {
        List<String> tags = new ArrayList<>();
        Product product = pv.getProduct();
        long days = (now.getTime() - product.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24);

        if (days <= 30) tags.add("New Arrival");
        if (soldQty >= 50) tags.add("Best Seller");
        if (currentStockQty > 0 && currentStockQty <= 10) tags.add("Limited");
        if (soldQty >= 100) tags.add("Signature");

        return tags;
    }

    private boolean filterDTO(ProductDTO dto, String[] categoryFilters, String[] gemstoneFilters, String[] metalFilters, Double maxPrice) {
        if (dto.getPrice() > maxPrice) return false;
        if (categoryFilters.length > 0 && Arrays.stream(categoryFilters).noneMatch(f -> f.equalsIgnoreCase(dto.getCategory()))) return false;
        if (gemstoneFilters.length > 0 && Arrays.stream(gemstoneFilters).noneMatch(f -> f.equalsIgnoreCase(dto.getGemstone()))) return false;
        if (metalFilters.length > 0 && Arrays.stream(metalFilters).noneMatch(f -> f.equalsIgnoreCase(dto.getColor()))) return false;
        return true;
    }
}