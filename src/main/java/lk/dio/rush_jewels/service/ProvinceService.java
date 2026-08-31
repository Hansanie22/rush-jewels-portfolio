package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.Province;
import lk.dio.rush_jewels.repository.ProvinceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProvinceService {

    private final ProvinceRepository provinceRepository;

    public ProvinceService(ProvinceRepository provinceRepository) {
        this.provinceRepository = provinceRepository;
    }

    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }

    public Province getProvinceById(Integer id) {
        return provinceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Province not found"));
    }

    // --- NEW: Method to find provinces by country ---
    public List<Province> getProvincesByCountry(Integer countryId) {
        return provinceRepository.findByCountryId(countryId);
    }
}