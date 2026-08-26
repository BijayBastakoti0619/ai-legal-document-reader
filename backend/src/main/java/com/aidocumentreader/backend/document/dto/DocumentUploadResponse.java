package com.aidocumentreader.backend.document.dto;

import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.entity.DocumentType;

import java.time.Instant;

public record DocumentUploadResponse(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        DocumentType documentType,
        DocumentStatus status,
        Instant createdAt
) {
}
