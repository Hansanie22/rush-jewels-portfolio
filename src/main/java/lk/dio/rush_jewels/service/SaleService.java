package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.dto.ItemSearchDTO;
import lk.dio.rush_jewels.dto.SaleDTO;
import lk.dio.rush_jewels.dto.SeasonalSaleBannerDTO;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.model.ProductVariance;
import lk.dio.rush_jewels.model.Sale;
import lk.dio.rush_jewels.repository.CollectionRepository;
import lk.dio.rush_jewels.repository.ProductVarianceRepository;
import lk.dio.rush_jewels.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductVarianceRepository productVarianceRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Transactional(readOnly = true)
    public List<SaleDTO> getAllActiveSales() {
        return saleRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemSearchDTO> searchItems(String query) {
        List<ItemSearchDTO> results = new ArrayList<>();

        for (ProductVariance pv : productVarianceRepository.searchActiveProducts(query)) {
            if (pv.getProduct() != null) {
                results.add(new ItemSearchDTO(
                        pv.getId(),
                        buildProductName(pv),
                        // ✅ CHANGE: Use Cloudinary URL directly
                        pv.getProduct().getImage1(),
                        "SKU-" + pv.getId(),
                        "PRODUCT"
                ));
            }
        }

        for (Collection c : collectionRepository.searchActiveCollections(query)) {
            results.add(new ItemSearchDTO(
                    c.getId(),
                    c.getName(),
                    // ✅ CHANGE: Use Cloudinary URL directly
                    c.getImage1(),
                    "COL-" + c.getId(),
                    "COLLECTION"
            ));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public SeasonalSaleBannerDTO getLatestSeasonalSaleBanner() {
        Optional<Sale> saleOpt = saleRepository.findFirstByIsActiveTrueOrderByIdDesc();

        if (saleOpt.isPresent()) {
            Sale sale = saleOpt.get();
            String imageUrl = "https://placehold.co/800x600?text=Sale";

            if (sale.getProductVariance() != null && sale.getProductVariance().getProduct() != null) {
                // ✅ CHANGE: Use Cloudinary URL directly
                imageUrl = sale.getProductVariance().getProduct().getImage1();
            } else if (sale.getCollection() != null) {
                // ✅ CHANGE: Use Cloudinary URL directly
                imageUrl = sale.getCollection().getImage1();
            }

            return new SeasonalSaleBannerDTO(sale.getDescription(), imageUrl, true);
        }

        return new SeasonalSaleBannerDTO(
                "Enjoy up to 30% off on selected items. Limited time offer.",
                "https://images.unsplash.com/photo-1515562141207-7a88fb7ce338?auto=format&fit=crop&w=800&q=80",
                false
        );
    }

    @Transactional
    public SaleDTO saveSale(SaleDTO dto) {
        Sale sale;

        if (dto.getId() > 0) {
            sale = saleRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
        } else {
            sale = new Sale();
        }

        sale.setDescription(dto.getDescription());
        sale.setStartDate(LocalDate.parse(dto.getStartDate()));
        sale.setEndDate(LocalDate.parse(dto.getEndDate()));
        sale.setActive(dto.isActive());

        if ("PRODUCT".equalsIgnoreCase(dto.getType())) {
            ProductVariance pv = productVarianceRepository.findById(dto.getProductVarianceId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            sale.setProductVariance(pv);
            sale.setCollection(null);

        } else if ("COLLECTION".equalsIgnoreCase(dto.getType())) {
            Collection col = collectionRepository.findById(dto.getCollectionId())
                    .orElseThrow(() -> new RuntimeException("Collection not found"));
            sale.setCollection(col);
            sale.setProductVariance(null);
        }

        return convertToDTO(saleRepository.save(sale));
    }


    @Transactional
    public void deleteSale(int id) {
        saleRepository.findById(id).ifPresent(sale -> {
            sale.setActive(false);
            saleRepository.save(sale);
        });
    }

    private SaleDTO convertToDTO(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setActive(sale.isActive());
        dto.setDescription(sale.getDescription());
        dto.setStartDate(sale.getStartDate() != null ? sale.getStartDate().toString() : null);
        dto.setEndDate(sale.getEndDate() != null ? sale.getEndDate().toString() : null);


        if (sale.getProductVariance() != null) {
            ProductVariance pv = sale.getProductVariance();
            dto.setType("PRODUCT");
            dto.setProductVarianceId(pv.getId());
            dto.setItemName(buildProductName(pv));
            dto.setItemSku("SKU-" + pv.getId());

            if (pv.getProduct() != null) {
                // ✅ CHANGE: Use Cloudinary URL directly
                dto.setItemImage(pv.getProduct().getImage1());
            }

        } else if (sale.getCollection() != null) {
            Collection c = sale.getCollection();
            dto.setType("COLLECTION");
            dto.setCollectionId(c.getId());
            dto.setItemName(c.getName());
            dto.setItemSku("COL-" + c.getId());

            // ✅ CHANGE: Use Cloudinary URL directly
            dto.setItemImage(c.getImage1());
        }

        return dto;
    }

    private String buildProductName(ProductVariance pv) {
        if (pv.getProduct() == null) return "Unknown Product";

        List<String> details = new ArrayList<>();

        if (pv.getColor() != null && pv.getColor().getColor() != null)
            details.add(pv.getColor().getColor());

        if (pv.getGemstone() != null && pv.getGemstone().getGemStone() != null)
            details.add(pv.getGemstone().getGemStone());

        if (pv.getSize() != null && pv.getSize().getSize() != null)
            details.add(pv.getSize().getSize());

        String base = pv.getProduct().getName();

        return details.isEmpty() ? base : base + " (" + String.join(", ", details) + ")";
    }
}