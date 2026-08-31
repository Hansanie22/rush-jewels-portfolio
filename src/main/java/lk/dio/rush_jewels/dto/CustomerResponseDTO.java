package lk.dio.rush_jewels.dto;

public class CustomerResponseDTO {
    private int id;
    private String name; // Combined First + Last Name
    private String email;
    private double totalSpent;
    private long orderCount;
    private int statusId;
    private String statusName;

    public CustomerResponseDTO(int id, String name, String email, double totalSpent, long orderCount, int statusId, String statusName) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.totalSpent = totalSpent;
        this.orderCount = orderCount;
        this.statusId = statusId;
        this.statusName = statusName;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public double getTotalSpent() { return totalSpent; }
    public long getOrderCount() { return orderCount; }
    public int getStatusId() { return statusId; }
    public String getStatusName() { return statusName; }
}