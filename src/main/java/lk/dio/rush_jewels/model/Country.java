package lk.dio.rush_jewels.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "country")
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // This is the full country name, e.g., "Sri Lanka"
    @Column(name = "country", length = 100, nullable = false)
    private String country;

    // --- ADDED THIS FIELD ---
    // This is the unique code you requested, e.g., "LK", "US"
    @Column(name = "code", length = 45, nullable = false, unique = true)
    private String code;

    // Constructors
    public Country() {}

    // Updated constructor
    public Country(String country, String code) {
        this.country = country;
        this.code = code;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // --- GETTERS AND SETTERS FOR NEW FIELD ---
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}