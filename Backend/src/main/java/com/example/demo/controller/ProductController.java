package com.example.demo.controller;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.model.ProductStatus;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

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
                minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null,
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

    @Autowired
    private ProductService productService;

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
}