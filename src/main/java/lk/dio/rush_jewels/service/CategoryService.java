package lk.dio.rush_jewels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.AdminCategoryDTO;
import lk.dio.rush_jewels.model.AdminAuditLog;
import lk.dio.rush_jewels.model.Category;
import lk.dio.rush_jewels.model.Product;
import lk.dio.rush_jewels.repository.AdminAuditLogRepository;
import lk.dio.rush_jewels.repository.CategoryRepository;
import lk.dio.rush_jewels.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AdminAuditLogRepository auditLogRepository; // Add repository
    private final ObjectMapper objectMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           AdminAuditLogRepository auditLogRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    public List<CategoryRepository.CategoryWithProductCount> getAllCategories() {
        return categoryRepository.findAllWithProductCount();
    }

    public CategoryRepository.CategoryWithProductCount getCategoryById(int id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found with id: " + id);
        }
        CategoryRepository.CategoryWithProductCount projection = categoryRepository.findWithProductCountById(id);
        if (projection == null) {
            throw new RuntimeException("Category projection not found with id: " + id);
        }
        return projection;
    }

    public Category getRawCategoryById(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    @Transactional
    public CategoryRepository.CategoryWithProductCount saveCategory(Category incoming) {
        // Check for duplicate category name (case-insensitive)
        boolean exists = categoryRepository.existsByCategoryIgnoreCase(incoming.getCategory());
        if (exists && incoming.getId() == 0) {
            throw new RuntimeException("Category '" + incoming.getCategory() + "' already exists.");
        }

        Category existing;
        String oldValue = null;

        if (incoming.getId() != 0) {
            existing = categoryRepository.findById(incoming.getId()).orElse(new Category());
            try {
                oldValue = objectMapper.writeValueAsString(existing);
            } catch (JsonProcessingException e) {
                oldValue = null;
            }
        } else {
            existing = new Category();
        }

        if (incoming.getCategory() != null) {
            existing.setCategory(incoming.getCategory());
        }
        existing.setStatusId(incoming.getStatusId());

        Category saved = categoryRepository.save(existing);
        categoryRepository.flush();

        // Audit log
        try {
            String newValue = objectMapper.writeValueAsString(saved);
            String actionType = (incoming.getId() != 0) ? "UPDATE" : "CREATE";
            AdminAuditLog log = new AdminAuditLog(
                    actionType,
                    "category",
                    String.valueOf(saved.getId()),
                    oldValue,
                    newValue,
                    null
            );
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CategoryRepository.CategoryWithProductCount result = categoryRepository.findWithProductCountById(saved.getId());
        if (result == null) {
            throw new RuntimeException("Failed to retrieve saved category with id: " + saved.getId());
        }

        return result;
    }

    @Transactional
    public CategoryRepository.CategoryWithProductCount toggleStatus(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        String oldValue = null;
        try {
            oldValue = objectMapper.writeValueAsString(category);
        } catch (JsonProcessingException ignored) {}

        int newStatus = category.getStatusId() == 1 ? 2 : 1;
        category.setStatusId(newStatus);

        Category saved = categoryRepository.save(category);
        categoryRepository.flush();

        // Audit log
        try {
            String newValue = objectMapper.writeValueAsString(saved);
            AdminAuditLog log = new AdminAuditLog(
                    "STATUS_TOGGLE",
                    "category",
                    String.valueOf(saved.getId()),
                    oldValue,
                    newValue,
                    null
            );
            auditLogRepository.save(log);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CategoryRepository.CategoryWithProductCount result = categoryRepository.findWithProductCountById(id);
        if (result == null) {
            throw new RuntimeException("Failed to retrieve category after status toggle with id: " + id);
        }

        return result;
    }

    public List<AdminCategoryDTO> getProductsByCategory(int categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }

        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private AdminCategoryDTO convertToDTO(Product product) {
        AdminCategoryDTO dto = new AdminCategoryDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setSpecifications(product.getSpecifications());
        dto.setWarranty(product.getWarranty());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getCategory());
        }

        if (product.getStatus() != null) {
            dto.setStatusId(product.getStatus().getId());
            dto.setStatusName(product.getStatus().getStatus());
        }

        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }
}
