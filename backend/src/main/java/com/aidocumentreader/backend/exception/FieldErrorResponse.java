package com.aidocumentreader.backend.exception;

public record FieldErrorResponse (
    String field,
    String message
)
{}
