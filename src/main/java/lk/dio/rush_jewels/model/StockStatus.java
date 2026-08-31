package lk.dio.rush_jewels.model; // Updated package to match the project structure

import jakarta.persistence.*; // Changed imports from javax.persistence
import java.io.Serializable;

@Entity
@Table(name = "stock_status")
public class StockStatus implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "stock_status", length = 45, nullable = false)
    private String stockStatus;

    // 🧱 Constructors
    public StockStatus() {
    }

    public StockStatus(int id, String stockStatus) {
        this.id = id;
        this.stockStatus = stockStatus;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}