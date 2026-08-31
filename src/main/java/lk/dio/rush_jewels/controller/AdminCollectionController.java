package lk.dio.rush_jewels.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.dio.rush_jewels.dto.CollectionRequestDTO;
import lk.dio.rush_jewels.dto.CollectionResponseDTO;
import lk.dio.rush_jewels.model.Collection;
import lk.dio.rush_jewels.service.AdminCollectionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/collections")
public class AdminCollectionController {

    private final AdminCollectionService collectionService;
    private final ObjectMapper objectMapper;

    public AdminCollectionController(AdminCollectionService collectionService, ObjectMapper objectMapper) {
        this.collectionService = collectionService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponseDTO>> getAllCollections() {
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponseDTO> getCollection(@PathVariable int id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCollection(
            @RequestPart("collection") String collectionJson,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @RequestPart(value = "image3", required = false) MultipartFile image3,
            @RequestPart(value = "image4", required = false) MultipartFile image4
    ) throws IOException {
        CollectionRequestDTO dto = objectMapper.readValue(collectionJson, CollectionRequestDTO.class);
        Collection saved = collectionService.saveCollection(dto);
        collectionService.saveCollectionImages(saved.getId(), image1, image2, image3, image4);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCollection(
            @PathVariable int id,
            @RequestPart("collection") String collectionJson,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @RequestPart(value = "image3", required = false) MultipartFile image3,
            @RequestPart(value = "image4", required = false) MultipartFile image4
    ) throws IOException {
        CollectionRequestDTO dto = objectMapper.readValue(collectionJson, CollectionRequestDTO.class);
        Collection saved = collectionService.updateCollection(id, dto);
        collectionService.saveCollectionImages(saved.getId(), image1, image2, image3, image4);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody StatusUpdateRequest request) {
        collectionService.updateStatus(id, request.isActive());
        return ResponseEntity.ok().build();
    }

    public static class StatusUpdateRequest {
        private boolean active;
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}