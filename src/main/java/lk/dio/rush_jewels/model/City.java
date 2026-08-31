package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "city")
public class City implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45, nullable = false)
    private String city;

    // --- THIS IS THE MISSING PIECE ---
    @ManyToOne
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;
    // ----------------------------------

    // 🧱 Constructors
    public City() {}

    public City(int id, String city) {
        this.id = id;
        this.city = city;
    }

    // 🧩 Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // --- ADD GETTERS/SETTERS FOR PROVINCE ---
    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }
    // ----------------------------------------
}