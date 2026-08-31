package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.dto.AdminCategoryDTO
        ;
import lk.dio.rush_jewels.model.Category;
import lk.dio.rush_jewels.repository.CategoryRepository;
import lk.dio.rush_jewels.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryRepository.CategoryWithProductCount>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable int id) {
        try {
            CategoryRepository.CategoryWithProductCount category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{id}/raw")
    public ResponseEntity<?> getRawCategory(@PathVariable int id) {
        try {
            Category category = categoryService.getRawCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            CategoryRepository.CategoryWithProductCount created = categoryService.saveCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create category: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateCategory(@PathVariable int id, @RequestBody Category category) {
        category.setId(id);
        try {
            CategoryRepository.CategoryWithProductCount updated = categoryService.saveCategory(category);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable int id) {
        try {
            CategoryRepository.CategoryWithProductCount result = categoryService.toggleStatus(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getCategoryProducts(@PathVariable int id) {
        try {
            List<AdminCategoryDTO> products = categoryService.getProductsByCategory(id);
            return ResponseEntity.ok(products);
        } catch (RuntimeException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}