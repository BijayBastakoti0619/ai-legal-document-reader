package com.aidocumentreader.backend.auth.dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn
) {}