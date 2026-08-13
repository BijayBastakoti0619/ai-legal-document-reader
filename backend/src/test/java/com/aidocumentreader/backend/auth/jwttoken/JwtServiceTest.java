package com.aidocumentreader.backend.auth.jwttoken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest{
    private JwtService jwtService;

    @BeforeEach
    void setUp(){
        jwtService = new JwtService("VGhpcy1pcy1hLXN1cGVyLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbnMtc2VjdXJpdHk=", 900000);
    }

    @Test
    void shouldGenerateValidTokenAndExtractEmail(){
        String userEmail = "testuser@example.com";
        String token = jwtService.generateAccessToken(userEmail);
        String extractEmail = jwtService.extractEmail(token);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(extractEmail).isEqualTo(userEmail);
    }
}