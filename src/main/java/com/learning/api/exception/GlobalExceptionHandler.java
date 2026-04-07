package com.learning.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 全局異常處理器
 * 統一處理各種異常並回傳友善的錯誤訊息
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理 IllegalArgumentException
     * 例如：Email 已註冊、帳號密碼錯誤等
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    /**
     * 處理 Bean Validation 錯誤
     * 例如：@NotBlank, @Email, @Size 等註解的驗證失敗
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 如果只有一個錯誤，直接回傳 message
        if (errors.size() == 1) {
            String message = errors.values().iterator().next();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", message));
        }

        // 多個錯誤時回傳完整的 errors map
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("errors", errors));
    }

    /**
     * 處理其他未預期的異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {
        e.printStackTrace();  // 在 console 印出完整錯誤資訊供除錯
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "系統發生錯誤，請稍後再試"));
    }
}