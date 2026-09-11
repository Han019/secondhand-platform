package com.secondhand.platform.common.exception;

import com.secondhand.platform.common.exception.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatedUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicatedUserException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(e.getMessage(), "DUPLICATED_USER")
        );
    }
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponse> handleLoginFailed(LoginFailedException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ErrorResponse(e.getMessage(), "LOGIN_FAILED")
        );
    }


}
