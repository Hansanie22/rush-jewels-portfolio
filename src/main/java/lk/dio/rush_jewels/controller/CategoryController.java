package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.model.Category;
import lk.dio.rush_jewels.repository.CategoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        // CHANGE: Use findByStatusId(1) instead of findAll()
        // This ensures only Active categories are sent to the frontend
        return categoryRepository.findByStatusId(1);
    }
}