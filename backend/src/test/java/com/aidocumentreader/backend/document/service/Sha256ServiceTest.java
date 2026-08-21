package com.aidocumentreader.backend.document.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256ServiceTest {

    private Sha256Service sha256Service;

    @BeforeEach
    void setUp() {
        sha256Service = new Sha256Service();
    }

    @Test
    void calculate_shouldGenerateCorrectSha256Hash() {

        byte[] content =
                "hello".getBytes(StandardCharsets.UTF_8);

        String hash = sha256Service.calculate(content);

        assertEquals(
                "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                hash
        );
    }

    @Test
    void calculate_shouldReturn64CharacterLowercaseHash() {

        byte[] content =
                "%PDF-1.7\nTest PDF"
                        .getBytes(StandardCharsets.UTF_8);

        String hash = sha256Service.calculate(content);

        assertEquals(64, hash.length());
        assertEquals(hash.toLowerCase(), hash);
    }
}