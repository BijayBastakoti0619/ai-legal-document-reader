package com.aidocumentreader.backend.document.dto;

import java.time.Instant;

public record DocumentSummaryResponse(
        Long id,
        String originalFilename,
        Long fileSize,
        String documentType,
        String status,
        Instant createdAt
) {}