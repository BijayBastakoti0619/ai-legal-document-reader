package com.aidocumentreader.backend.document.dto;

import java.time.Instant;

public record DocumentDetailResponse(
        Long id,
        String originalFilename,
        String contentType,
        Long fileSize,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}