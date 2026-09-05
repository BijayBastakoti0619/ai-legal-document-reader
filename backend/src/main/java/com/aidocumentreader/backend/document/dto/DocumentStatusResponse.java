package com.aidocumentreader.backend.document.dto;

public record DocumentStatusResponse(
        Long documentId,
        String status,
        String failureCode,
        String message // Ticket explicitly requested "message" instead of "failureMessage"
) {}