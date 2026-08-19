package com.aidocumentreader.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.b2")
public record B2StorageProperties(
        String bucket,
        String region,
        String endpoint,
        String keyId,
        String applicationKey
) {
}
