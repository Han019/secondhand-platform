package com.secondhand.platform.common.exception.dto;

public record ErrorResponse(
        String message,
        String errorCode
) {
}
