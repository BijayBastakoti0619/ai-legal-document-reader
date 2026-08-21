package com.aidocumentreader.backend.user.service;

import com.aidocumentreader.backend.config.B2StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class B2StorageService {

    private final S3Client s3Client;
    private final B2StorageProperties properties;

    public void upload(
            String objectKey,
            byte[] fileBytes,
            String contentType
    ) {

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromBytes(fileBytes)
        );
    }
}
