package com.secondhand.platform.productimage;

import com.secondhand.platform.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(AccessLevel.PROTECTED)
@Table(name ="images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String imagePath;
    private Integer sortOrder;
}
