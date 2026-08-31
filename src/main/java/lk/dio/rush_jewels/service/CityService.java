package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.City;
import lk.dio.rush_jewels.repository.CityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public City getCityById(Integer id) {
        return cityRepository.findById(id).orElseThrow(() -> new RuntimeException("City not found"));
    }

    // --- NEW: Method to find cities by province ---
    public List<City> getCitiesByProvince(Integer provinceId) {
        return cityRepository.findByProvinceId(provinceId);
    }
}