package lk.dio.rush_jewels.controller;

import lk.dio.rush_jewels.service.ExcelService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products/bulk")
public class BulkProductController {

    private final ExcelService excelService;

    public BulkProductController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        ByteArrayInputStream stream = excelService.generateTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=products_template.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            excelService.processExcelUpload(file);
            response.put("message", "Products uploaded successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
