package com.secondhand.platform.common.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message,Long productId) {
        super(message + " productId: " + productId);
    }
}
