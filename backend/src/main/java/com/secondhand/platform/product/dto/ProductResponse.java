package com.secondhand.platform.product.dto;

import com.secondhand.platform.product.Product;
import com.secondhand.platform.product.ProductStatus;

public record ProductResponse(
        Long id,
        String title,
        Long price,
        ProductStatus status,
        String address,
        String imageUrl
) {
    public static ProductResponse from(Product product,String imageUrl) {
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getStatus(),
                product.getAddress(),
                imageUrl
        );
    }
}
