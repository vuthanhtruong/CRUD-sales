package com.example.demo.controller;

import com.example.demo.dto.ExportFileDTO;
import com.example.demo.dto.PageResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.model.ProductStatus;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/page")
    public PageResponseDTO<ProductDTO> findProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String productTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return productService.findProductsPage(
                keyword,
                minPrice,
                maxPrice,
                productTypeId,
                status,
                page,
                pageSize
        );
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportProducts(
            @RequestParam(defaultValue = "excel") String format,
            @RequestParam(defaultValue = "current") String scope,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String pages,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String productTypeId,
            @RequestParam(required = false) String status
    ) {
        List<Integer> selectedPages = parsePages(pages);
        ExportFileDTO file = productService.exportProducts(
                format,
                scope,
                page,
                pageSize,
                selectedPages,
                keyword,
                minPrice,
                maxPrice,
                productTypeId,
                status
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.getFileName()).build().toString())
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .contentLength(file.getData().length)
                .body(file.getData());
    }

    private List<Integer> parsePages(String pages) {
        if (pages == null || pages.isBlank()) return List.of();
        return Arrays.stream(pages.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .filter(page -> page > 0)
                .distinct()
                .sorted()
                .toList();
    }

    @GetMapping("/search")
    public List<ProductDTO> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String productTypeId,
            @RequestParam(required = false) String status
    ) {
        return productService.searchProducts(
                keyword,
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                productTypeId,
                status
        );
    }

    @GetMapping("/paged")
    public List<ProductDTO> findAllPaged(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        pageSize = Math.max(1, Math.min(pageSize, 100));
        page = Math.max(1, page);
        return productService.findAllPaged(page, pageSize);
    }

    @GetMapping("/total-pages")
    public int countTotalPages(
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        pageSize = Math.max(1, Math.min(pageSize, 100));
        return productService.countTotalPages(pageSize);
    }

    @GetMapping
    public List<ProductDTO> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductDTO findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @GetMapping("/count")
    public long countProducts() {
        return productService.countProducts();
    }

    @PostMapping
    public void create(@RequestBody ProductDTO dto) {
        productService.create(dto);
    }

    @PutMapping("/{id}")
    public void edit(@PathVariable String id, @RequestBody ProductDTO dto) {
        productService.edit(dto, id);
    }

    @DeleteMapping("/{productId}")
    public void delete(@PathVariable String productId) {
        productService.delete(productId);
    }

    @GetMapping("/statuses")
    public ProductStatus[] getStatuses() {
        return ProductStatus.values();
    }

    @GetMapping("/exists-by-type/{productTypeId}")
    public boolean existsByProductType(@PathVariable String productTypeId) {
        return productService.existsByProductType(productTypeId);
    }

    @GetMapping("/exists-by-color/{colorId}")
    public boolean existsByColor(@PathVariable String colorId) {
        return productService.existsByColorId(colorId);
    }

    @GetMapping("/exists-by-size/{sizeId}")
    public boolean existsBySize(@PathVariable String sizeId) {
        return productService.existsBySizeId(sizeId);
    }
}
