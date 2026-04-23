package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantId implements Serializable {

    private String productId;
    private String sizeId;
    private String colorId;
}