package com.secondhand.platform.product.dto;

import com.secondhand.platform.product.Product;
import com.secondhand.platform.product.ProductStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long id,
        String title,
        String description,
        Long price,
        ProductStatus status,
        Double latitude,
        Double longitude,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        SellerResponse seller,
        List<ImageResponse> images
) {
    public static ProductDetailResponse from(Product product, List<ImageResponse> images) {
        return new ProductDetailResponse(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStatus(),
                product.getLatitude(),
                product.getLongitude(),
                product.getAddress(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                SellerResponse.from(product),
                images
        );
    }

    public record ImageResponse(Long id, String url) {
    }

    public record SellerResponse(
            Long id,
            String nickname,
            String profileImage,
            double mannerScore
    ) {
        private static SellerResponse from(Product product) {
            return new SellerResponse(
                    product.getSeller().getId(),
                    product.getSeller().getNickname(),
                    product.getSeller().getProfileImage(),
                    product.getSeller().getMannerScore()
            );
        }
    }
}
