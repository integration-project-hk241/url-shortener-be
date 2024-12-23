package org.url.urlshortenerbe.exceptions;

import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.url.urlshortenerbe.dtos.responses.ErrorResponse;
import org.url.urlshortenerbe.dtos.responses.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    ResponseEntity<Response<Void>> handleException(Exception ex) {
        log.error("Exception: {}", ex.toString());

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .build();

        Response<Void> response =
                Response.<Void>builder().success(false).error(errorResponse).build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AppException.class)
    ResponseEntity<Response<Void>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        Response<Void> response =
                Response.<Void>builder().success(false).error(errorResponse).build();

        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Response<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String enumKey = Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        Response<Void> response =
                Response.<Void>builder().success(false).error(errorResponse).build();

        return ResponseEntity.badRequest().body(response);
    }
}
