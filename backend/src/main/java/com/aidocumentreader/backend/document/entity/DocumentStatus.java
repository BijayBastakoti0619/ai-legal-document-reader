package com.aidocumentreader.backend.document.entity;

public enum DocumentStatus {
    UPLOADED,
    EXTRACTING,
    EXTRACTED,
    ANALYZING,
    COMPLETED,
    FAILED,
    DELETED
}
