package com.secondhand.platform.common.exception;

public class DuplicatedUserException extends RuntimeException{
    public DuplicatedUserException(String message) {
        super(message);
    }
}
