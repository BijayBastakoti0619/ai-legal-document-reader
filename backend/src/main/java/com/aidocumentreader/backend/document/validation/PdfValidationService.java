package com.aidocumentreader.backend.document.validation;

import com.aidocumentreader.backend.exception.DocumentValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

@Service
public class PdfValidationService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private static final byte[] PDF_SIGNATURE =
            "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final long maxFileSizeBytes;

    public PdfValidationService(
            @Value("${app.document-upload.max-file-size}")
            DataSize maxFileSize
    ) {
        this.maxFileSizeBytes = maxFileSize.toBytes();
    }

    public void validate(MultipartFile file) {

        validateNotEmpty(file);
        validateFileSize(file);
        validateExtension(file);
        validateContentType(file);
        validatePdfSignature(file);
    }

    private void validateNotEmpty(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new DocumentValidationException(
                    HttpStatus.BAD_REQUEST,
                    "EMPTY_FILE",
                    "Please select a non-empty PDF file."
            );
        }
    }

    private void validateFileSize(MultipartFile file) {

        if (file.getSize() > maxFileSizeBytes) {
            throw new DocumentValidationException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "FILE_TOO_LARGE",
                    "The uploaded PDF exceeds the maximum allowed file size."
            );
        }
    }

    private void validateExtension(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();

        if (!StringUtils.hasText(originalFilename)) {
            throw new DocumentValidationException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_TYPE",
                    "The uploaded file must have a .pdf extension."
            );
        }

        String cleanedFilename = StringUtils.cleanPath(originalFilename);

        if (!cleanedFilename
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf")) {

            throw new DocumentValidationException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_TYPE",
                    "The uploaded file must have a .pdf extension."
            );
        }
    }

    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null
                || !PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)) {

            throw new DocumentValidationException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_TYPE",
                    "The uploaded file must have the application/pdf content type."
            );
        }
    }

    private void validatePdfSignature(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            byte[] header =
                    inputStream.readNBytes(PDF_SIGNATURE.length);

            if (!Arrays.equals(header, PDF_SIGNATURE)) {
                throw new DocumentValidationException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_PDF",
                        "The uploaded file is not a valid PDF."
                );
            }

        } catch (IOException exception) {

            throw new DocumentValidationException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_PDF",
                    "The uploaded PDF could not be read."
            );
        }
    }
}
