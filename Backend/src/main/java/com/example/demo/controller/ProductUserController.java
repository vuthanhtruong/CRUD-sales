package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.ProductImageService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/user")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductUserController {

    @Autowired
    private ProductVariantService productVariantService;

    @Autowired
    private ProductImageService productImageService;

    private final ProductService productService;

    public ProductUserController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/page")
    public ResponseEntity<PageResponseDTO<ProductUserDTO>> searchProductsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String productTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int pageSize
    ) {
        return ResponseEntity.ok(productService.searchUserProductsPage(
                keyword,
                minPrice,
                maxPrice,
                productTypeId,
                page,
                pageSize
        ));
    }

    @GetMapping("/{productId}/sizes")
    public List<SizeDTO> getSizesByProduct(@PathVariable String productId) {
        return productVariantService.findSizesByProductId(productId);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductUserDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false) String productTypeId
    ) {
        List<ProductUserDTO> result = productService.searchUserProducts(
                keyword,
                minPrice,
                maxPrice,
                productTypeId
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductUserDTO>> filterProductsByPrice(
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice
    ) {
        List<ProductUserDTO> result;

        if (minPrice != null && maxPrice != null) {
            result = productService.findUserProductsByPriceBetween(minPrice, maxPrice);
        } else if (minPrice != null) {
            result = productService.findUserProductsByPriceGreaterThan(minPrice);
        } else if (maxPrice != null) {
            result = productService.findUserProductsByPriceLessThan(maxPrice);
        } else {
            result = productService.getProductsForUser();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{productId}/colors")
    public List<ColorDTO> getColorsByProduct(@PathVariable String productId) {
        return productVariantService.findColorsByProductId(productId);
    }

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDTO>> getImagesByProduct(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(
                productImageService.findByProductId(productId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductUserDTO>> getProductsForUser() {
        List<ProductUserDTO> products = productService.getProductsForUser();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String productId) {
        return ResponseEntity.ok(
                productService.findById(productId)
        );
    }
}
