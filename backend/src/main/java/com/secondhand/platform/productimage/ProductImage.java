package com.secondhand.platform.productimage;

import com.secondhand.platform.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    public ProductImage(
            Product product,
            String imagePath,
            Integer sortOrder
    ) {
       this.product = product;
       this.imagePath = imagePath;
       this.sortOrder = sortOrder;
    }
}
