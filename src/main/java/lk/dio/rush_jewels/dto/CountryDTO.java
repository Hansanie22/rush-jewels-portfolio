package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.Country;

public class CountryDTO {
    private int id;
    private String country;

    public CountryDTO(Country country) {
        this.id = country.getId();
        this.country = country.getCountry();
    }
    // Add public getters
    public int getId() { return id; }
    public String getCountry() { return country; }
}