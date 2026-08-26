package com.aidocumentreader.backend.exception;

import org.springframework.http.HttpStatus;

public class DocumentValidationException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public DocumentValidationException(
            HttpStatus status,
            String code,
            String message
    ) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
