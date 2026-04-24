package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductVariantId implements Serializable {

    @Column(name = "product_id")
    private String productId;

    @Column(name = "size_id")
    private String sizeId;

    @Column(name = "color_id")
    private String colorId;

    // constructor, equals, hashCode
}