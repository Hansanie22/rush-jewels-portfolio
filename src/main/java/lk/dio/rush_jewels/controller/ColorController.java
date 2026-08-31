package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Color;
import lk.dio.rush_jewels.repository.ColorRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
public class ColorController {

    private final ColorRepository colorRepository;

    public ColorController(ColorRepository colorRepository) {
        this.colorRepository = colorRepository;
    }

    @GetMapping
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }
}
