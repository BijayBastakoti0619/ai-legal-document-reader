package com.aidocumentreader.backend.user.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        String role
) {}