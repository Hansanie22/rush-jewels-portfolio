package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.City;

public class CityDTO {
    private int id;
    private String city;

    public CityDTO(City city) {
        this.id = city.getId();
        this.city = city.getCity();
    }
    // Add public getters
    public int getId() { return id; }
    public String getCity() { return city; }
}