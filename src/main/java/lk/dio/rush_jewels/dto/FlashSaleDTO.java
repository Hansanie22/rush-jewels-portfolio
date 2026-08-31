package lk.dio.rush_jewels.dto;

import java.time.LocalDate;

public class FlashSaleDTO {

    private String name;
    private Double discountPercentage;
    private LocalDate endDate;
    private String description;

    public FlashSaleDTO(String name, Double discountPercentage, LocalDate endDate, String description) {
        this.name = name;
        this.discountPercentage = discountPercentage;
        this.endDate = endDate;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Double getDiscountPercentage() {
        return discountPercentage;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }
}
