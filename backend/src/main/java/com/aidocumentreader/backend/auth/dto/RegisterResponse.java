package com.aidocumentreader.backend.auth.dto;

import com.aidocumentreader.backend.user.entity.Role;

public record RegisterResponse(
        Long id,
        String email,
        String displayName,
        Role role
) {
}
