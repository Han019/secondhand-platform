package com.secondhand.platform.common.exception;

public class ProductAccessDeniedException extends RuntimeException {
    public ProductAccessDeniedException(String message) {
        super(message);
    }
}
