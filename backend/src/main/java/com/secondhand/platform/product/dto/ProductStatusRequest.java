package com.secondhand.platform.product.dto;

import com.secondhand.platform.product.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
        @NotNull(message = "판매 상태는 필수")
        ProductStatus status
) {
}
