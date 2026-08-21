package com.aidocumentreader.backend.user.controller;

import com.aidocumentreader.backend.user.service.B2StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageTestController {

    private final B2StorageService storageService;

    @PostMapping("/test")
    public ResponseEntity<String> testUpload() {

        String objectKey =
                "test/" + UUID.randomUUID() + ".txt";

        byte[] content =
                "Hello from Spring Boot and Backblaze B2"
                        .getBytes(StandardCharsets.UTF_8);

        storageService.upload(
                objectKey,
                content,
                "text/plain"
        );

        return ResponseEntity.ok(
                "Uploaded successfully: " + objectKey
        );
    }
}
