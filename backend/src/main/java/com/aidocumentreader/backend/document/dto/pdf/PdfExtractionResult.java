package com.aidocumentreader.backend.document.dto.pdf;

import java.util.List;

public record PdfExtractionResult(
        List<ExtractedPage> pages,
        String combinedText
) {
}
