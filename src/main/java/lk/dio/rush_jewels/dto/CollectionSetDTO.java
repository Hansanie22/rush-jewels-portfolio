package lk.dio.rush_jewels.dto;

import java.util.List;

public class CollectionSetDTO {
    private int collectionId;
    private String collectionName;
    private int totalItems;
    private List<SetItemDTO> items;

    // ✅ No-Args Constructor (Optional, but good for frameworks)
    public CollectionSetDTO() {
    }

    // ✅ Parameterized Constructor (REQUIRED for your Service code)
    public CollectionSetDTO(int collectionId, String collectionName, int totalItems, List<SetItemDTO> items) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.totalItems = totalItems;
        this.items = items;
    }

    // Getters and Setters
    public int getCollectionId() { return collectionId; }
    public void setCollectionId(int collectionId) { this.collectionId = collectionId; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
    public List<SetItemDTO> getItems() { return items; }
    public void setItems(List<SetItemDTO> items) { this.items = items; }
}