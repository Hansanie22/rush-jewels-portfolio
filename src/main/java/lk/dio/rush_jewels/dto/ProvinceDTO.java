package lk.dio.rush_jewels.dto;

import lk.dio.rush_jewels.model.Province;

public class ProvinceDTO {
    private int id;
    private String province;

    public ProvinceDTO(Province province) {
        this.id = province.getId();
        this.province = province.getProvince();
    }
    // Add public getters
    public int getId() { return id; }
    public String getProvince() { return province; }
}