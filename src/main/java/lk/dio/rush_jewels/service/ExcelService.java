package lk.dio.rush_jewels.service;

import lk.dio.rush_jewels.model.*;
import lk.dio.rush_jewels.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import lk.dio.rush_jewels.model.Color;

@Service
public class ExcelService {

    private final ProductRepository productRepository;
    private final ProductVarianceRepository productVarianceRepository;
    private final StockRepository stockRepository;
    private final CategoryRepository categoryRepository;
    private final ColorRepository colorRepository;
    private final GemstoneRepository gemstoneRepository;
    private final SizeRepository sizeRepository;
    private final StatusRepository statusRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockStatusRepository stockStatusRepository;

    public ExcelService(ProductRepository productRepository,
                        ProductVarianceRepository productVarianceRepository,
                        StockRepository stockRepository,
                        CategoryRepository categoryRepository,
                        ColorRepository colorRepository,
                        GemstoneRepository gemstoneRepository,
                        SizeRepository sizeRepository,
                        StatusRepository statusRepository,
                        WarehouseRepository warehouseRepository,
                        StockStatusRepository stockStatusRepository) {
        this.productRepository = productRepository;
        this.productVarianceRepository = productVarianceRepository;
        this.stockRepository = stockRepository;
        this.categoryRepository = categoryRepository;
        this.colorRepository = colorRepository;
        this.gemstoneRepository = gemstoneRepository;
        this.sizeRepository = sizeRepository;
        this.statusRepository = statusRepository;
        this.warehouseRepository = warehouseRepository;
        this.stockStatusRepository = stockStatusRepository;
    }

    public ByteArrayInputStream generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Name", "Title", "Description", "Specifications", "Warranty", "Category", 
                                "Metal(Color)", "Gemstone", "Size", "Price", "StockLimit", "Qty"};

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
            }

            // Dummy Row
            Row dummyRow = sheet.createRow(1);
            dummyRow.createCell(0).setCellValue("Pearl Drop Earrings");
            dummyRow.createCell(1).setCellValue("Elegant Pearl Drop Earrings");
            dummyRow.createCell(2).setCellValue("A beautiful set of pearl earrings.");
            dummyRow.createCell(3).setCellValue("Weight: 5g");
            dummyRow.createCell(4).setCellValue("1 Year");
            dummyRow.createCell(5).setCellValue("Earrings");
            dummyRow.createCell(6).setCellValue("Gold");
            dummyRow.createCell(7).setCellValue("Pearl");
            dummyRow.createCell(8).setCellValue("Standard");
            dummyRow.createCell(9).setCellValue(15000);
            dummyRow.createCell(10).setCellValue(10);
            dummyRow.createCell(11).setCellValue(50);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel template: " + e.getMessage());
        }
    }

    @Transactional
    public void processExcelUpload(MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) { // Skip Header
                    rowNumber++;
                    continue;
                }

                // Parse Row Data
                String name = getCellString(currentRow.getCell(0));
                if (name == null || name.isEmpty()) break; // End of data

                String title = getCellString(currentRow.getCell(1));
                String desc = getCellString(currentRow.getCell(2));
                String specs = getCellString(currentRow.getCell(3));
                String warranty = getCellString(currentRow.getCell(4));
                String catName = getCellString(currentRow.getCell(5));
                String colorName = getCellString(currentRow.getCell(6));
                String gemName = getCellString(currentRow.getCell(7));
                String sizeName = getCellString(currentRow.getCell(8));
                
                double price = currentRow.getCell(9) != null ? currentRow.getCell(9).getNumericCellValue() : 0.0;
                int stockLimit = currentRow.getCell(10) != null ? (int) currentRow.getCell(10).getNumericCellValue() : 0;
                int qty = currentRow.getCell(11) != null ? (int) currentRow.getCell(11).getNumericCellValue() : 0;

                // Lookup Entities
                Category category = categoryRepository.findByCategory(catName).orElse(null);
                if(category == null) {
                    category = new Category();
                    category.setCategory(catName != null ? catName : "Uncategorized");
                    Status status = statusRepository.findById(1).orElse(null);
                    // category.setStatus(status); // Removed because Category might not have setStatus
                    category = categoryRepository.save(category);
                }

                Color color = colorRepository.findByColor(colorName).orElse(null);
                Gemstone gem = gemstoneRepository.findByGemStone(gemName).orElse(null);
                Size size = sizeRepository.findBySize(sizeName).stream().findFirst().orElse(null);
                Status activeStatus = statusRepository.findById(1).orElse(null);

                // Create Product
                Product product = new Product();
                product.setName(name);
                product.setTitle(title != null ? title : name);
                product.setDescription(desc != null ? desc : "");
                product.setSpecifications(specs != null ? specs : "");
                product.setWarranty(warranty != null ? warranty : "None");
                product.setCategory(category);
                product.setStatus(activeStatus);
                product.setCreatedAt(new Date());
                product = productRepository.save(product);

                // Create Variance
                ProductVariance variance = new ProductVariance();
                variance.setProduct(product);
                variance.setColor(color);
                variance.setGemstone(gem);
                variance.setSize(size);
                variance.setPrice(price);
                variance.setRegularPrice(price);
                variance.setStockLimit(stockLimit);
                variance.setDiscountPercentage(0.0);
                variance.setStatus(activeStatus);
                variance = productVarianceRepository.save(variance);

                // Create Stock
                Stock stock = new Stock();
                stock.setQty(qty);
                stock.setProductVariance(variance);
                stock.setWarehouse(warehouseRepository.findById(1).orElse(null)); // Default Main Warehouse
                stock.setStockStatus(stockStatusRepository.findById(1).orElse(null)); // Default IN_STOCK
                stockRepository.save(stock);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }
}
