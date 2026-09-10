package com.aidocumentreader.backend.document.service;

import com.aidocumentreader.backend.document.dto.pdf.PdfExtractionResult;
import com.aidocumentreader.backend.exception.PdfExtractionErrorCode;
import com.aidocumentreader.backend.exception.PdfExtractionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PdfTextExtractionServiceTest {

    private PdfTextExtractionService service;

    @BeforeEach
    void setUp() {
        service = new PdfTextExtractionService();
    }

    @Test
    void shouldExtractTextFromNormalPdf() throws Exception {

        byte[] pdf = createTextPdf(
                "This is a legal document."
        );

        PdfExtractionResult result = service.extract(pdf);

        assertNotNull(result);
        assertEquals(1, result.pages().size());
        assertEquals(1, result.pages().getFirst().pageNumber());

        assertTrue(
                result.pages()
                        .getFirst()
                        .text()
                        .contains("This is a legal document.")
        );

        assertTrue(
                result.combinedText()
                        .contains("This is a legal document.")
        );
    }

    @Test
    void shouldExtractTextFromMultiplePages() throws Exception {

        byte[] pdf = createTextPdf(
                "Content from page one.",
                "Content from page two."
        );

        PdfExtractionResult result = service.extract(pdf);

        assertEquals(2, result.pages().size());

        assertEquals(
                1,
                result.pages().get(0).pageNumber()
        );

        assertEquals(
                2,
                result.pages().get(1).pageNumber()
        );

        assertTrue(
                result.pages().get(0).text()
                        .contains("Content from page one.")
        );

        assertTrue(
                result.pages().get(1).text()
                        .contains("Content from page two.")
        );

        assertTrue(
                result.combinedText()
                        .contains("Content from page one.")
        );

        assertTrue(
                result.combinedText()
                        .contains("Content from page two.")
        );
    }

    @Test
    void shouldRejectEmptyByteArray() {

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(new byte[0])
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_EMPTY,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldRejectNullPdf() {

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(null)
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_EMPTY,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldRejectCorruptPdf() {

        byte[] corruptPdf =
                "This is definitely not a PDF."
                        .getBytes(StandardCharsets.UTF_8);

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(corruptPdf)
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_CORRUPT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldRejectEncryptedPdf() throws Exception {

        byte[] encryptedPdf = createEncryptedPdf();

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(encryptedPdf)
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_ENCRYPTED,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldRejectPdfWithNoExtractableText() throws Exception {

        byte[] pdf = createBlankPdf();

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(pdf)
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_NO_EXTRACTABLE_TEXT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldDetectImageOnlyPdf() throws Exception {

        byte[] pdf = createImageOnlyPdf();

        PdfExtractionException exception =
                assertThrows(
                        PdfExtractionException.class,
                        () -> service.extract(pdf)
                );

        assertEquals(
                PdfExtractionErrorCode.PDF_SCANNED_OR_IMAGE_ONLY,
                exception.getErrorCode()
        );
    }


    // --------------------------------------------------
    // Test PDF helpers
    // --------------------------------------------------

    private byte[] createTextPdf(String... pageTexts)
            throws Exception {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PDType1Font font =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            for (String text : pageTexts) {

                PDPage page = new PDPage();

                document.addPage(page);

                try (PDPageContentStream contentStream =
                             new PDPageContentStream(
                                     document,
                                     page
                             )) {

                    contentStream.beginText();

                    contentStream.setFont(
                            font,
                            12
                    );

                    contentStream.newLineAtOffset(
                            50,
                            700
                    );

                    contentStream.showText(text);

                    contentStream.endText();
                }
            }

            document.save(output);

            return output.toByteArray();
        }
    }

    private byte[] createBlankPdf()
            throws Exception {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            document.addPage(
                    new PDPage()
            );

            document.save(output);

            return output.toByteArray();
        }
    }

    private byte[] createEncryptedPdf()
            throws Exception {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PDPage page = new PDPage();

            document.addPage(page);

            PDType1Font font =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                contentStream.beginText();

                contentStream.setFont(
                        font,
                        12
                );

                contentStream.newLineAtOffset(
                        50,
                        700
                );

                contentStream.showText(
                        "Secret legal document"
                );

                contentStream.endText();
            }

            AccessPermission permission =
                    new AccessPermission();

            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(
                            "ownerPassword",
                            "userPassword",
                            permission
                    );

            policy.setEncryptionKeyLength(128);

            document.protect(policy);

            document.save(output);

            return output.toByteArray();
        }
    }

    private byte[] createImageOnlyPdf()
            throws Exception {

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PDPage page = new PDPage();

            document.addPage(page);

            BufferedImage image =
                    new BufferedImage(
                            200,
                            200,
                            BufferedImage.TYPE_INT_RGB
                    );

            PDImageXObject pdfImage =
                    LosslessFactory.createFromImage(
                            document,
                            image
                    );

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(
                                 document,
                                 page
                         )) {

                contentStream.drawImage(
                        pdfImage,
                        100,
                        500,
                        200,
                        200
                );
            }

            document.save(output);

            return output.toByteArray();
        }
    }
}
