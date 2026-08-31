package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.SearchProductDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final ProductVarianceRepository productVarianceRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final GemstoneRepository gemstoneRepository;
    private final CollectionRepository collectionRepository;
    private final OrderItemsRepository orderItemsRepository;

    // ✅ NOTE: Image Base Paths removed.
    // We now use Cloudinary URLs directly from the database.

    private static final int ACTIVE_STATUS_ID = 1;
    private static final int AVAILABLE_STOCK_STATUS_ID = 1;

    public SearchService(ProductVarianceRepository productVarianceRepository,
                         StockRepository stockRepository,
                         ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         GemstoneRepository gemstoneRepository,
                         CollectionRepository collectionRepository,
                         OrderItemsRepository orderItemsRepository) {
        this.productVarianceRepository = productVarianceRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.gemstoneRepository = gemstoneRepository;
        this.collectionRepository = collectionRepository;
        this.orderItemsRepository = orderItemsRepository;
    }

    // ====================================================================
    // 🔍 Mixed Search Logic
    // ====================================================================

    public List<SearchProductDTO> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String searchText = query.trim();
        List<SearchProductDTO> results = new ArrayList<>();

        // 1. ID Search
        try {
            int id = Integer.parseInt(searchText);
            Optional<ProductVariance> pvOpt = productVarianceRepository.findById(id);
            if (pvOpt.isPresent() && pvOpt.get().getProduct() != null) {
                List<ProductVariance> variances = productVarianceRepository.findByProduct_Id(pvOpt.get().getProduct().getId());
                results.addAll(variances.stream().filter(this::isAvailableInStock).map(this::mapProductToDTO).toList());
            }
            Optional<Collection> colOpt = collectionRepository.findById(id);
            if (colOpt.isPresent() && colOpt.get().getStatus().getId() == ACTIVE_STATUS_ID) {
                results.add(mapCollectionToDTO(colOpt.get()));
            }
        } catch (NumberFormatException ignored) {}

        // 2. Text Search - Products
        List<ProductVariance> productResults = productVarianceRepository.findActiveByTextSearch(searchText);
        results.addAll(productResults.stream().filter(this::isAvailableInStock).map(this::mapProductToDTO).toList());

        // 3. Text Search - Collections
        List<Collection> collectionResults = collectionRepository.findActiveByTextSearch(searchText);
        results.addAll(collectionResults.stream().map(this::mapCollectionToDTO).toList());

        return results.stream()
                .distinct()
                .filter(distinctByKey(dto -> dto.getType() + "-" + (dto.getType().equals("PRODUCT") ? dto.getVarianceId() : dto.getProductId())))
                .collect(Collectors.toList());
    }

    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        Set<Object> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private boolean isAvailableInStock(ProductVariance pv) {
        return stockRepository.existsByProductVarianceAndStockStatus_IdAndQtyGreaterThan(pv, AVAILABLE_STOCK_STATUS_ID, 0);
    }

    // --- Mappers ---
    private SearchProductDTO mapProductToDTO(ProductVariance v) {
        SearchProductDTO item = new SearchProductDTO();
        Product product = v.getProduct();
        double finalPrice = v.getRegularPrice();
        if (v.getDiscountPercentage() != null && v.getDiscountPercentage() > 0) {
            finalPrice = v.getRegularPrice() * (1 - v.getDiscountPercentage() / 100);
        }
        item.setPrice(finalPrice);
        int stockQty = stockRepository.findAvailableStockByProductVariance(v).map(Stock::getQty).orElse(0);
        item.setStockQty(stockQty);
        String displayName = product.getName();
        if (v.getSize() != null && v.getSize().getSize() != null && !v.getSize().getSize().isEmpty()) {
            displayName += " (Size: " + v.getSize().getSize() + ")";
        }
        item.setName(displayName);
        item.setVarianceId(v.getId());
        item.setProductId(product.getId());
        item.setTitle(product.getTitle());
        item.setDescription(product.getDescription());
        item.setRegularPrice(v.getRegularPrice());
        item.setDiscountPercentage(v.getDiscountPercentage() != null ? v.getDiscountPercentage() : 0.0);
        item.setSize(v.getSize() != null ? v.getSize().getSize() : "");
        item.setColor(v.getColor() != null ? v.getColor().getColor() : "");
        item.setGemstone(v.getGemstone() != null ? v.getGemstone().getGemStone() : "");
        item.setCategory(product.getCategory() != null ? product.getCategory().getCategory() : "");
        item.setType("PRODUCT");

        // ✅ CHANGE: Use Cloudinary URL directly
        item.setImage(product.getImage1());

        return item;
    }

    private SearchProductDTO mapProductBaseToDTO(Product product) {
        SearchProductDTO item = new SearchProductDTO();
        item.setProductId(product.getId());
        item.setName(product.getName());
        item.setType("PRODUCT");
        item.setImage(product.getImage1());
        return item;
    }

    private SearchProductDTO mapCollectionToDTO(Collection c) {
        SearchProductDTO item = new SearchProductDTO();
        item.setProductId(c.getId());
        item.setVarianceId(c.getId());
        item.setName(c.getName() + " (Collection)");
        item.setTitle(c.getTitle());
        item.setDescription(c.getDescription());
        item.setPrice(c.getPrice());
        item.setRegularPrice(c.getRegularPrice());
        item.setDiscountPercentage(c.getDiscountPercentage());
        item.setStockQty(c.getStockLimit());
        item.setStockLimit(c.getStockLimit());
        item.setCategory("Collection");
        item.setGemstone("");
        item.setSize("");
        item.setColor("");
        item.setType("COLLECTION");

        // ✅ CHANGE: Use Cloudinary URL directly
        item.setImage(c.getImage1());

        return item;
    }

    // ====================================================================
    // 💡 SearchSuggestions Logic
    // ====================================================================

    public Map<String, List<SearchProductDTO>> getSearchSuggestions(String query) {
        Map<String, List<SearchProductDTO>> result = new LinkedHashMap<>();

        if (query == null || query.trim().isEmpty()) {
            // --- SCENARIO 1: No Input (Show Recommendations) ---

            // 1. Best Sellers
            PageRequest pageRequest = PageRequest.of(0, 5);
            List<SearchProductDTO> bestSellers = orderItemsRepository.findBestSellingProductNames(pageRequest).stream()
                    .map(name -> {
                        SearchProductDTO dto = new SearchProductDTO();
                        dto.setName(name);
                        dto.setType("PRODUCT_NAME");
                        return dto;
                    }).collect(Collectors.toList());
            if (!bestSellers.isEmpty()) {
                result.put("Best Sellers", bestSellers);
            }

            // 2. New Arrivals (In Stock)
            PageRequest stockRequest = PageRequest.of(0, 10);
            List<SearchProductDTO> inStockProducts = productRepository.findActiveInStockProducts(stockRequest)
                    .stream().map(this::mapProductBaseToDTO).collect(Collectors.toList());
            if (!inStockProducts.isEmpty()) {
                result.put("In Stock Products", inStockProducts);
            }

            // 3. Collections
            List<SearchProductDTO> collections = collectionRepository.findByStatus_IdOrderByIdDesc(ACTIVE_STATUS_ID, pageRequest)
                    .stream().map(this::mapCollectionToDTO).collect(Collectors.toList());
            if (!collections.isEmpty()) {
                result.put("Collections", collections);
            }

        } else {
            // --- SCENARIO 2: User Typing (Show Matches) ---
            String searchQ = query.trim();
            PageRequest limit = PageRequest.of(0, 5);

            // 1. Matching Products
            List<SearchProductDTO> productMatches = productRepository.findProductsByQuery(searchQ, limit)
                    .stream().map(this::mapProductBaseToDTO).collect(Collectors.toList());
            if (!productMatches.isEmpty()) {
                result.put("Products", productMatches);
            }

            // 2. Matching Collections
            List<SearchProductDTO> collectionMatches = collectionRepository.findCollectionsByQuery(searchQ, limit)
                    .stream().map(this::mapCollectionToDTO).collect(Collectors.toList());
            if (!collectionMatches.isEmpty()) {
                result.put("Collections", collectionMatches);
            }
        }

        return result;
    }
}