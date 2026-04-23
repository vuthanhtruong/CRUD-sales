package com.example.demo.controller;

import com.example.demo.dto.ProductTypeDTO;
import com.example.demo.service.ProductTypueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-types")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductTypeController {

    @Autowired
    private ProductTypueService productTypueService;

    @GetMapping
    public List<ProductTypeDTO> getAllProductTypes() {
        return productTypueService.getProductTypes();
    }

    @GetMapping("/{id}")
    public ProductTypeDTO getProductTypeById(@PathVariable String id) {
        return productTypueService.getProductTypeById(id);
    }

    @PostMapping
    public void createProductType(@RequestBody ProductTypeDTO productTypeDTO) {
        productTypueService.createProductType(productTypeDTO);
    }

    @PutMapping("/{id}")
    public void updateProductType(@PathVariable String id,
                                  @RequestBody ProductTypeDTO productTypeDTO) {
        productTypueService.updateProductType(productTypeDTO, id);
    }

    @DeleteMapping("/{id}")
    public void deleteProductType(@PathVariable String id) {
        productTypueService.deleteProductType(id);
    }
}