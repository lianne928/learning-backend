package com.learning.api.exception;

import com.learning.api.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> IllegalArgumentException(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
    }

}
