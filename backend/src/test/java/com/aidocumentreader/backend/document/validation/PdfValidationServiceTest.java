package com.aidocumentreader.backend.document.validation;

import com.aidocumentreader.backend.exception.DocumentValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PdfValidationServiceTest {

    private PdfValidationService pdfValidationService;

    @BeforeEach
    void setUp() {
        pdfValidationService =
                new PdfValidationService(DataSize.ofMegabytes(10));
    }

    @Test
    void validate_shouldAcceptValidPdf() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "%PDF-1.7\nTest PDF content"
                        .getBytes(StandardCharsets.US_ASCII)
        );

        assertDoesNotThrow(() ->
                pdfValidationService.validate(file)
        );
    }

    @Test
    void validate_shouldRejectEmptyFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[0]
        );

        DocumentValidationException exception =
                assertThrows(
                        DocumentValidationException.class,
                        () -> pdfValidationService.validate(file)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatus()
        );

        assertEquals(
                "EMPTY_FILE",
                exception.getCode()
        );
    }

    @Test
    void validate_shouldRejectFileLargerThanMaximumSize() {

        byte[] oversizedContent =
                new byte[(int) DataSize.ofMegabytes(10).toBytes() + 1];

        byte[] pdfSignature =
                "%PDF-".getBytes(StandardCharsets.US_ASCII);

        System.arraycopy(
                pdfSignature,
                0,
                oversizedContent,
                0,
                pdfSignature.length
        );

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                oversizedContent
        );

        DocumentValidationException exception =
                assertThrows(
                        DocumentValidationException.class,
                        () -> pdfValidationService.validate(file)
                );

        assertEquals(
                HttpStatus.PAYLOAD_TOO_LARGE,
                exception.getStatus()
        );

        assertEquals(
                "FILE_TOO_LARGE",
                exception.getCode()
        );
    }

    @Test
    void validate_shouldRejectInvalidExtension() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "application/pdf",
                "%PDF-1.7\nTest content"
                        .getBytes(StandardCharsets.US_ASCII)
        );

        DocumentValidationException exception =
                assertThrows(
                        DocumentValidationException.class,
                        () -> pdfValidationService.validate(file)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatus()
        );

        assertEquals(
                "INVALID_FILE_TYPE",
                exception.getCode()
        );
    }

    @Test
    void validate_shouldRejectInvalidContentType() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "text/plain",
                "%PDF-1.7\nTest content"
                        .getBytes(StandardCharsets.US_ASCII)
        );

        DocumentValidationException exception =
                assertThrows(
                        DocumentValidationException.class,
                        () -> pdfValidationService.validate(file)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatus()
        );

        assertEquals(
                "INVALID_FILE_TYPE",
                exception.getCode()
        );
    }

    @Test
    void validate_shouldRejectFakePdfContent() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "This is not actually a PDF."
                        .getBytes(StandardCharsets.UTF_8)
        );

        DocumentValidationException exception =
                assertThrows(
                        DocumentValidationException.class,
                        () -> pdfValidationService.validate(file)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatus()
        );

        assertEquals(
                "INVALID_PDF",
                exception.getCode()
        );
    }
}