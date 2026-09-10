package com.aidocumentreader.backend.exception;
public enum PdfExtractionErrorCode {

    PDF_CORRUPT(
            "This PDF appears to be damaged or invalid."
    ),

    PDF_ENCRYPTED(
            "Password-protected PDFs are not supported."
    ),

    PDF_EMPTY(
            "This PDF does not contain any readable content."
    ),

    PDF_NO_EXTRACTABLE_TEXT(
            "We couldn't extract readable text from this PDF."
    ),

    PDF_SCANNED_OR_IMAGE_ONLY(
            "This document appears to contain scanned images instead of selectable text."
    ),

    PDF_EXTRACTION_FAILED(
            "We couldn't process this PDF. Please try another document."
    );

    private final String userMessage;

    PdfExtractionErrorCode(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}