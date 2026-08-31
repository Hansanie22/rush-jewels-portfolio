package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.Banner;
import lk.dio.rush_jewels.repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderByIdAsc();
    }
}