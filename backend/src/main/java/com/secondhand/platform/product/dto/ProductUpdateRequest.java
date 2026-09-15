package com.secondhand.platform.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductUpdateRequest(
        @NotBlank(message = "상품 제목은 필수")
        String title,
        @NotBlank(message = "상품 설명은 필수")
        String description,
        @NotNull(message = "상품 가격은 필수")
        @PositiveOrZero(message = "상품 가격은 0원 이상")
        Long price
) {
}
