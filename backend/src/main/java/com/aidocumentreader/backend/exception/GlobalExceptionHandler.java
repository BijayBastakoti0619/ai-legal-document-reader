package com.aidocumentreader.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                correlationId(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> fieldErrors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new FieldErrorResponse(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "One or more fields are invalid.",
                request.getRequestURI(),
                correlationId(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred.",
                request.getRequestURI(),
                correlationId(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private String correlationId() {
        String existingId = MDC.get("correlationId");

        return existingId != null
                ? existingId
                : UUID.randomUUID().toString();
    }
}