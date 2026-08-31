package lk.dio.rush_jewels.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // ✅ Import this
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "warehouse")
public class Warehouse implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String warehouse;

    @JsonIgnore
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Stock> stockList;

    public Warehouse() {}

    public Warehouse(int id, String warehouse, List<Stock> stockList) {
        this.id = id;
        this.warehouse = warehouse;
        this.stockList = stockList;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public List<Stock> getStockList() { return stockList; }
    public void setStockList(List<Stock> stockList) { this.stockList = stockList; }
}