package com.aidocumentreader.backend.document.dto;

import com.aidocumentreader.backend.document.entity.DocumentStatus;

import java.time.Instant;

public record DocumentUploadResponse(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        DocumentStatus status,
        Instant createdAt
) {
}
