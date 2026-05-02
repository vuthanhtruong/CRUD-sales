package com.example.demo.service;

import com.example.demo.dto.ExportFileDTO;
import com.example.demo.dto.PageResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductUserDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.ProductStatus;
import com.example.demo.model.ProductType;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductImageDAO;
import com.example.demo.repository.ProductTypeDAO;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProductImageDAO productImageDAO;

    @Autowired
    private ProductTypeDAO productType;

    @Override
    public boolean existsByColorId(String colorId) {
        return productDAO.existsByColorId(colorId);
    }

    @Override
    public boolean existsBySizeId(String sizeId) {
        return productDAO.existsBySizeId(sizeId);
    }

    @Override
    public boolean existsByProductType(String productTypeId) {
        return productDAO.existsByProductType(productTypeId);
    }

    // ==================== ADMIN ====================

    @Override
    @Cacheable(value = "products",
            key = "'search:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId + ':' + #status")
    public List<ProductDTO> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    ) {
        validatePriceRange(minPrice, maxPrice);
        ProductStatus productStatus = parseStatus(status);

        return productDAO.searchProductsAdminDTO(
                keyword, minPrice, maxPrice, productTypeId, productStatus
        );
    }

    @Override
    @Cacheable(value = "products",
            key = "'page:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId + ':' + #status + ':' + #page + ':' + #pageSize")
    public PageResponseDTO<ProductDTO> findProductsPage(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status,
            int page,
            int pageSize
    ) {
        validatePriceRange(minPrice, maxPrice);
        ProductStatus productStatus = parseStatus(status);
        pageSize = clampPageSize(pageSize);
        page = Math.max(1, page);

        long totalItems = productDAO.countSearchProductsAdmin(
                keyword, minPrice, maxPrice, productTypeId, productStatus
        );
        int totalPages = calculateTotalPages(totalItems, pageSize);
        page = Math.min(page, totalPages);

        List<ProductDTO> content = productDAO.searchProductsAdminPagedDTO(
                keyword, minPrice, maxPrice, productTypeId, productStatus, page, pageSize
        );

        return new PageResponseDTO<>(
                content,
                page,
                pageSize,
                totalItems,
                totalPages,
                page > 1,
                page < totalPages
        );
    }

    @Override
    @Cacheable(value = "products", key = "'paged:' + #page + ':' + #pageSize")
    public List<ProductDTO> findAllPaged(int page, int pageSize) {
        pageSize = clampPageSize(pageSize);
        page = Math.max(1, page);

        return productDAO.findAllPagedDTO(page, pageSize);
    }

    @Override
    @Cacheable(value = "products", key = "'totalPages:' + #pageSize")
    public int countTotalPages(int pageSize) {
        pageSize = clampPageSize(pageSize);
        long total = productDAO.countProducts();
        return calculateTotalPages(total, pageSize);
    }

    @Override
    @Cacheable(value = "products", key = "'count'")
    public long countProducts() {
        return productDAO.countProducts();
    }

    @Override
    @Cacheable(value = "products", key = "'all'")
    public List<ProductDTO> findAll() {
        return productDAO.findAllDTO();
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDTO findById(String id) {
        return productDAO.findByIdDTO(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void create(ProductDTO dto) {
        Product product = toEntity(dto);
        productDAO.create(product);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();

            for (var imgDTO : dto.getImages()) {
                if (imgDTO.getImageData() == null || imgDTO.getImageData().isBlank()) {
                    throw new RuntimeException("Image data cannot be empty");
                }

                ProductImage img = new ProductImage();
                img.setImageData(java.util.Base64.getDecoder().decode(imgDTO.getImageData()));
                img.setContentType(imgDTO.getContentType());
                img.setPrimary(imgDTO.isPrimary());
                img.setProduct(product);
                images.add(img);
            }

            productImageDAO.createBatch(images);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void edit(ProductDTO dto, String id) {
        Product product = toEntity(dto);
        productDAO.edit(product, id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void delete(String id) {
        productDAO.delete(id);
    }

    @Override
    public ExportFileDTO exportProducts(
            String format,
            String scope,
            Integer page,
            Integer pageSize,
            List<Integer> pages,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    ) {
        validatePriceRange(minPrice, maxPrice);
        ProductStatus productStatus = parseStatus(status);
        int safePageSize = clampPageSize(pageSize == null ? 10 : pageSize);
        String safeScope = normalize(scope, "current");
        String safeFormat = normalize(format, "excel");

        List<ProductDTO> data = new ArrayList<>();

        if ("all".equals(safeScope)) {
            data = productDAO.searchProductsAdminDTO(keyword, minPrice, maxPrice, productTypeId, productStatus);
        } else if ("selected".equals(safeScope)) {
            if (pages == null || pages.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one page to export");
            }
            for (Integer selectedPage : pages.stream().filter(p -> p != null && p > 0).distinct().sorted().toList()) {
                data.addAll(productDAO.searchProductsAdminPagedDTO(
                        keyword, minPrice, maxPrice, productTypeId, productStatus, selectedPage, safePageSize
                ));
            }
        } else {
            int safePage = Math.max(1, page == null ? 1 : page);
            data = productDAO.searchProductsAdminPagedDTO(
                    keyword, minPrice, maxPrice, productTypeId, productStatus, safePage, safePageSize
            );
        }

        return switch (safeFormat) {
            case "word", "docx" -> buildWordExport(data);
            case "pdf" -> buildPdfExport(data);
            case "excel", "xlsx" -> buildExcelExport(data);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }

    // ==================== USER ====================

    @Override
    @Cacheable(value = "userProducts",
            key = "'search:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId")
    public List<ProductUserDTO> searchUserProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
        validatePriceRange(minPrice, maxPrice);
        return productDAO.searchProductsDTO(keyword, minPrice, maxPrice, productTypeId);
    }

    @Override
    @Cacheable(value = "userProducts",
            key = "'page:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId + ':' + #page + ':' + #pageSize")
    public PageResponseDTO<ProductUserDTO> searchUserProductsPage(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            int page,
            int pageSize
    ) {
        validatePriceRange(minPrice, maxPrice);
        pageSize = clampPageSize(pageSize);
        page = Math.max(1, page);

        long totalItems = productDAO.countSearchProducts(keyword, minPrice, maxPrice, productTypeId);
        int totalPages = calculateTotalPages(totalItems, pageSize);
        page = Math.min(page, totalPages);

        List<ProductUserDTO> content = productDAO.searchProductsPagedDTO(
                keyword, minPrice, maxPrice, productTypeId, page, pageSize
        );

        return new PageResponseDTO<>(
                content,
                page,
                pageSize,
                totalItems,
                totalPages,
                page > 1,
                page < totalPages
        );
    }

    @Override
    @Cacheable(value = "userProducts", key = "'all'")
    public List<ProductUserDTO> getProductsForUser() {
        return productDAO.getProductsForUserDTO();
    }

    @Override
    @Cacheable(value = "userProducts", key = "'gt:' + #minPrice")
    public List<ProductUserDTO> findUserProductsByPriceGreaterThan(BigDecimal minPrice) {
        return productDAO.findByPriceGreaterThanDTO(minPrice);
    }

    @Override
    @Cacheable(value = "userProducts", key = "'between:' + #minPrice + ':' + #maxPrice")
    public List<ProductUserDTO> findUserProductsByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        validatePriceRange(minPrice, maxPrice);
        return productDAO.findByPriceBetweenDTO(minPrice, maxPrice);
    }

    @Override
    @Cacheable(value = "userProducts", key = "'lt:' + #maxPrice")
    public List<ProductUserDTO> findUserProductsByPriceLessThan(BigDecimal maxPrice) {
        return productDAO.findByPriceLessThanDTO(maxPrice);
    }

    // ==================== EXPORT HELPERS ====================

    private ExportFileDTO buildExcelExport(List<ProductDTO> products) {
        String[] headers = exportHeaders();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Products");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIndex = 1;
            for (ProductDTO product : products) {
                Row row = sheet.createRow(rowIndex++);
                List<String> values = exportRow(product);
                for (int i = 0; i < values.size(); i++) {
                    row.createCell(i).setCellValue(values.get(i));
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ExportFileDTO(
                    exportFileName("products", "xlsx"),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray()
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not export Excel file", e);
        }
    }

    private ExportFileDTO buildWordExport(List<ProductDTO> products) {
        String[] headers = exportHeaders();
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = title.createRun();
            run.setBold(true);
            run.setFontSize(18);
            run.setText("Product List");

            XWPFParagraph meta = document.createParagraph();
            meta.createRun().setText("Total products: " + products.size());

            XWPFTable table = document.createTable(1, headers.length);
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                setCellText(headerRow.getCell(i), headers[i], true);
            }

            for (ProductDTO product : products) {
                XWPFTableRow row = table.createRow();
                List<String> values = exportRow(product);
                for (int i = 0; i < values.size(); i++) {
                    setCellText(row.getCell(i), values.get(i), false);
                }
            }

            document.write(out);
            return new ExportFileDTO(
                    exportFileName("products", "docx"),
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    out.toByteArray()
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not export Word file", e);
        }
    }

    private ExportFileDTO buildPdfExport(List<ProductDTO> products) {
        String[] headers = exportHeaders();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            Paragraph title = new Paragraph("Product List", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);
            document.add(new Paragraph("Total products: " + products.size(), bodyFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.5f, 1.4f, 1.2f, 1.2f, 3.2f, 1.4f});

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (ProductDTO product : products) {
                for (String value : exportRow(product)) {
                    PdfPCell cell = new PdfPCell(new Phrase(value, bodyFont));
                    cell.setPadding(5);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

            return new ExportFileDTO(
                    exportFileName("products", "pdf"),
                    "application/pdf",
                    out.toByteArray()
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not export PDF file", e);
        }
    }

    private String[] exportHeaders() {
        return new String[]{"Product ID", "Product Name", "Type", "Status", "Price", "Description", "Created By"};
    }

    private List<String> exportRow(ProductDTO product) {
        return List.of(
                safe(product.getProductId()),
                safe(product.getProductName()),
                safe(product.getProductTypeId()),
                safe(product.getStatus()),
                product.getPrice() != null ? product.getPrice().toPlainString() : "",
                safe(product.getDescription()),
                safe(product.getCreatedBy())
        );
    }

    private void setCellText(XWPFTableCell cell, String text, boolean bold) {
        cell.removeParagraph(0);
        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.setBold(bold);
        run.setText(text == null ? "" : text);
    }

    private String exportFileName(String baseName, String extension) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return baseName + "_" + stamp + "." + extension;
    }

    // ==================== COMMON HELPERS ====================

    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }
    }

    private ProductStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return ProductStatus.valueOf(status.trim());
    }

    private int clampPageSize(int pageSize) {
        return Math.max(1, Math.min(pageSize, 100));
    }

    private int calculateTotalPages(long totalItems, int pageSize) {
        return Math.max(1, (int) Math.ceil((double) totalItems / pageSize));
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    // ==================== MAPPERS ====================

    private ProductDTO toDTO(Product product) {
        if (product == null) return null;

        String base64Image = null;

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                if (img.isPrimary()) {
                    base64Image = java.util.Base64.getEncoder()
                            .encodeToString(img.getImageData());
                    break;
                }
            }

            if (base64Image == null) {
                base64Image = java.util.Base64.getEncoder()
                        .encodeToString(product.getImages().get(0).getImageData());
            }
        }

        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getStatus() != null ? product.getStatus().name() : null,
                product.getProductType() != null ? product.getProductType().getId() : null,
                product.getCreatedBy() != null ? product.getCreatedBy().getId() : null,
                product.getPrice(),
                product.getDescription(),
                base64Image,
                null
        );
    }

    private ProductUserDTO toUserDTO(Product product) {
        ProductUserDTO dto = new ProductUserDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());

        String base64Image = null;

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                if (img.isPrimary()) {
                    base64Image = java.util.Base64.getEncoder().encodeToString(img.getImageData());
                    break;
                }
            }
            if (base64Image == null) {
                base64Image = java.util.Base64.getEncoder()
                        .encodeToString(product.getImages().get(0).getImageData());
            }
        }

        dto.setImage(base64Image);
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(dto.getStatus()));
        }

        if (dto.getProductTypeId() != null) {
            ProductType type = productType.getProductTypeById(dto.getProductTypeId());
            product.setProductType(type);
        }

        return product;
    }
}
