package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Gemstone;
import lk.dio.rush_jewels.repository.GemstoneRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gemstones")
public class GemstoneController {

    private final GemstoneRepository gemstoneRepository;

    public GemstoneController(GemstoneRepository gemstoneRepository) {
        this.gemstoneRepository = gemstoneRepository;
    }

    @GetMapping
    public List<Gemstone> getAllGemstones() {
        return gemstoneRepository.findAll();
    }
}
