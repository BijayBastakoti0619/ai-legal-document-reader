package com.aidocumentreader.backend.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        int expiresIn,
        UserInfo user
){
    public record UserInfo(
            Long id,
            String email,
            String displayName,
            String Role
    ){}
}