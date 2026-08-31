package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.CollectionRepository;
import lk.dio.rush_jewels.repository.CollectionSetRepository;
import lk.dio.rush_jewels.repository.StockRepository;
import lk.dio.rush_jewels.repository.ReviewRepository;
import lk.dio.rush_jewels.dto.CollectionDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final StockRepository stockRepository;
    private final CollectionSetRepository collectionSetRepository;
    private final ReviewRepository reviewRepository;

    private static final int ACTIVE_STATUS_ID = 1;
    private static final int WAREHOUSE_ID_MAIN = 1;
    private static final int OUT_OF_STOCK_STATUS_ID = 3;

    public CollectionService(CollectionRepository collectionRepository,
                             StockRepository stockRepository,
                             CollectionSetRepository collectionSetRepository,
                             ReviewRepository reviewRepository) {
        this.collectionRepository = collectionRepository;
        this.stockRepository = stockRepository;
        this.collectionSetRepository = collectionSetRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<CollectionDTO> getActiveCollections() {
        List<Collection> activeCollections = collectionRepository.findByStatus_Id(ACTIVE_STATUS_ID);
        return activeCollections.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CollectionDTO getCollectionById(int id) {
        return collectionRepository.findById(id)
                .map(entity -> {
                    CollectionDTO dto = mapToDTO(entity);

                    List<CollectionSet> sets = collectionSetRepository.findByCollection(entity);
                    if (sets != null && !sets.isEmpty()) {
                        List<CollectionDTO.CollectionItemDTO> items = sets.stream()
                                .map(set -> {
                                    ProductVariance pv = set.getProductVariance();
                                    Product p = pv.getProduct();

                                    StringBuilder variantDetails = new StringBuilder();
                                    if(pv.getColor() != null) variantDetails.append(pv.getColor().getColor()).append(" ");
                                    if(pv.getGemstone() != null) variantDetails.append(pv.getGemstone().getGemStone()).append(" ");

                                    String subtext = (pv.getSize() != null) ? pv.getSize().getSize() : "";
                                    String finalVariantName = variantDetails.length() > 0 ? variantDetails.toString().trim() : "Standard";

                                    // ✅ OLD: String prodImage = "/uploads" + productImageBasePath + "/" + p.getId() + "/image1.png";
                                    // ✅ NEW: කෙලින්ම Product එකේ Cloudinary URL එක ගන්නවා
                                    String prodImage = p.getImage1();

                                    return new CollectionDTO.CollectionItemDTO(
                                            p.getTitle(),
                                            finalVariantName,
                                            subtext,
                                            set.getQty(),
                                            prodImage
                                    );
                                })
                                .collect(Collectors.toList());
                        dto.setCollectionItems(items);
                    }
                    return dto;
                })
                .orElse(null);
    }

    public List<CollectionDTO> getRelatedCollections(int excludeId) {
        List<Collection> allActive = collectionRepository.findByStatus_Id(ACTIVE_STATUS_ID);
        Collections.shuffle(allActive);
        return allActive.stream()
                .filter(c -> c.getId() != excludeId)
                .limit(4)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CollectionDTO mapToDTO(Collection entity) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setRegularPrice(entity.getRegularPrice());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setPrice(entity.getPrice());
        dto.setTitle(entity.getTitle());
        dto.setSpecifications(entity.getSpecifications());
        dto.setWarranty(entity.getWarranty());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setMaterial(entity.getMaterial());

        // Stock Calculation
        List<Stock> stocks = stockRepository.findByCollection(entity);
        int totalQty = stocks.stream()
                .filter(s -> s.getWarehouse() != null
                        && s.getWarehouse().getId() == WAREHOUSE_ID_MAIN
                        && s.getStockStatus().getId() != OUT_OF_STOCK_STATUS_ID)
                .mapToInt(Stock::getQty)
                .sum();

        dto.setStockLimit(totalQty);
        dto.setStatus(totalQty > 0 ? "In Stock" : "Out Of Stock");

        // Fetch Reviews
        Double avgRating = reviewRepository.getAverageRatingByCollectionId(entity.getId());
        int reviewCount = reviewRepository.getReviewCountByCollectionId(entity.getId());

        dto.setAverageRating(avgRating != null ? avgRating : 0.0);
        dto.setReviewCount(reviewCount);

        // ✅ Main Image (Cloudinary URL)
        dto.setImage(entity.getImage1());

        // ✅ Gallery Images (Cloudinary URLs)
        List<String> galleryImages = new ArrayList<>();
        if (entity.getImage1() != null) galleryImages.add(entity.getImage1());
        if (entity.getImage2() != null) galleryImages.add(entity.getImage2());
        if (entity.getImage3() != null) galleryImages.add(entity.getImage3());
        if (entity.getImage4() != null) galleryImages.add(entity.getImage4());

        dto.setImages(galleryImages);

        return dto;
    }
}