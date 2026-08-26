package com.aidocumentreader.backend.document.dto;

public record DocumentContent(
        String filename,
        String contentType,
        byte[] bytes
) {

}
