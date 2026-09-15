package com.secondhand.platform.common.exception;

public class DuplicatedUserException extends RuntimeException{
    private final String errorCode;

    public DuplicatedUserException(String message, String errorCode) {

        super(message);
        this.errorCode = errorCode;
    }
}
