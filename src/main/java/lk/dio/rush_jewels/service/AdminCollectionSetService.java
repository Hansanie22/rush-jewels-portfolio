package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.CollectionSetDTO;
import lk.dio.rush_jewels.dto.SetItemDTO;
import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminCollectionSetService {

    private final CollectionSetRepository setRepository;
    private final CollectionRepository collectionRepository;
    private final ProductVarianceRepository varianceRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AdminCollectionSetService(CollectionSetRepository setRepository,
                                     CollectionRepository collectionRepository,
                                     ProductVarianceRepository varianceRepository,
                                     AdminAuditLogRepository auditLogRepository,
                                     ObjectMapper objectMapper) {
        this.setRepository = setRepository;
        this.collectionRepository = collectionRepository;
        this.varianceRepository = varianceRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public List<CollectionSetDTO> getAllSets() {
        List<CollectionSet> allRows = setRepository.findAll();
        Map<Integer, List<CollectionSet>> grouped = allRows.stream()
                .collect(Collectors.groupingBy(cs -> cs.getCollection().getId()));

        List<CollectionSetDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<CollectionSet>> entry : grouped.entrySet()) {
            List<CollectionSet> sets = entry.getValue();
            if (sets.isEmpty()) continue;
            String name = sets.get(0).getCollection().getTitle();
            List<SetItemDTO> items = sets.stream()
                    .map(cs -> new SetItemDTO(cs.getProductVariance().getId(), generateVarianceName(cs.getProductVariance()), cs.getQty()))
                    .collect(Collectors.toList());
            result.add(new CollectionSetDTO(entry.getKey(), name, items.size(), items));
        }
        return result;
    }

    public void saveCollectionSet(int collectionId, List<SetItemDTO> items) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        String oldValue = convertToJson(setRepository.findByCollection_Id(collectionId));

        // Clear existing items
        setRepository.deleteByCollection_Id(collectionId);

        // ✅ Update Type and Add Items
        if (items != null && !items.isEmpty()) {
            collection.setType("BUNDLE"); // Products තෝරා ඇති නිසා BUNDLE වේ.
            for (SetItemDTO item : items) {
                ProductVariance variance = varianceRepository.findById(item.getVarianceId())
                        .orElseThrow(() -> new RuntimeException("Variance not found: " + item.getVarianceId()));
                CollectionSet set = new CollectionSet();
                set.setCollection(collection);
                set.setProductVariance(variance);
                set.setQty(item.getQty());
                setRepository.save(set);
            }
        } else {
            collection.setType("STANDALONE"); // Items කිසිවක් නැති නිසා STANDALONE වේ.
        }

        collectionRepository.save(collection);
        logAction("UPDATE_SET", "collection_set", String.valueOf(collectionId), oldValue, items);
    }

    private String generateVarianceName(ProductVariance v) {
        StringBuilder sb = new StringBuilder(v.getProduct().getName());
        List<String> attrs = new ArrayList<>();
        if (v.getSize() != null) attrs.add(v.getSize().getSize());
        if (v.getColor() != null) attrs.add(v.getColor().getColor());
        if (v.getGemstone() != null) attrs.add(v.getGemstone().getGemStone());
        if (!attrs.isEmpty()) sb.append(" (").append(String.join(" / ", attrs)).append(")");
        return sb.toString();
    }

    private void logAction(String action, String table, String recordId, String old, Object newVal) {
        try {
            auditLogRepository.save(new AdminAuditLog(action, table, recordId, old, convertToJson(newVal), LocalDateTime.now()));
        } catch (Exception e) { System.err.println("Audit failed: " + e.getMessage()); }
    }

    private String convertToJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); } catch (Exception e) { return "[]"; }
    }
}