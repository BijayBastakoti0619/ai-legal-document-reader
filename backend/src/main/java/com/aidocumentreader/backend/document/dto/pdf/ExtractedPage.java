package com.aidocumentreader.backend.document.dto.pdf;

public record ExtractedPage(
        int pageNumber,
        String text
) {
}
