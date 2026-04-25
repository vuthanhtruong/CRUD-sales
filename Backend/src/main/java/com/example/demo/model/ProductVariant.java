package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_variant", indexes = {
        @Index(name = "idx_variant_product_quantity", columnList = "product_id, quantity"),
        @Index(name = "idx_variant_size_color", columnList = "size_id, color_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductVariant {

    @EmbeddedId
    private ProductVariantId id;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @MapsId("sizeId")
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @MapsId("colorId")
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(name = "quantity")
    private Integer quantity;
}