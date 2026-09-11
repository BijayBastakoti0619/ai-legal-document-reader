package com.aidocumentreader.backend.exception;

public class PdfExtractionException extends RuntimeException {

    private final PdfExtractionErrorCode errorCode;

    public PdfExtractionException(PdfExtractionErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public PdfExtractionErrorCode getErrorCode() {
        return errorCode;
    }
}
