package com.aidocumentreader.backend.document.service;

import com.aidocumentreader.backend.document.dto.pdf.ExtractedPage;
import com.aidocumentreader.backend.document.dto.pdf.PdfExtractionResult;
import com.aidocumentreader.backend.exception.PdfExtractionErrorCode;
import com.aidocumentreader.backend.exception.PdfExtractionException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextExtractionService {

    public PdfExtractionResult extract(byte[] pdfBytes) {

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new PdfExtractionException(
                    PdfExtractionErrorCode.PDF_EMPTY
            );
        }

        PDDocument document;

        // Step 1: Load PDF
        try {
            document = Loader.loadPDF(pdfBytes);
        } catch (InvalidPasswordException e) {
            throw new PdfExtractionException(
                    PdfExtractionErrorCode.PDF_ENCRYPTED
            );
        } catch (IOException e) {
            throw new PdfExtractionException(
                    PdfExtractionErrorCode.PDF_CORRUPT
            );
        }

        // Step 2: Extract
        try (document) {

            if (document.isEncrypted()) {
                throw new PdfExtractionException(
                        PdfExtractionErrorCode.PDF_ENCRYPTED
                );
            }

            if (document.getNumberOfPages() == 0) {
                throw new PdfExtractionException(
                        PdfExtractionErrorCode.PDF_EMPTY
                );
            }

            PDFTextStripper stripper = new PDFTextStripper();

            // Usually improves reading order
            stripper.setSortByPosition(true);

            List<ExtractedPage> pages = new ArrayList<>();

            StringBuilder combinedText = new StringBuilder();

            boolean containsImages = false;

            for (int pageNumber = 1;
                 pageNumber <= document.getNumberOfPages();
                 pageNumber++) {

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String rawText = stripper.getText(document);

                String normalizedText = normalizeText(rawText);

                pages.add(
                        new ExtractedPage(
                                pageNumber,
                                normalizedText
                        )
                );

                if (!normalizedText.isBlank()) {

                    if (!combinedText.isEmpty()) {
                        combinedText.append("\n\n");
                    }

                    combinedText.append(normalizedText);
                }

                PDPage page = document.getPage(pageNumber - 1);

                if (containsImage(page.getResources(), 0)) {
                    containsImages = true;
                }
            }

            String finalText = combinedText.toString().trim();

            /*
             * No readable text, but images exist:
             * very likely a scanned/image-only PDF.
             */
            if (finalText.isBlank() && containsImages) {
                throw new PdfExtractionException(
                        PdfExtractionErrorCode.PDF_SCANNED_OR_IMAGE_ONLY
                );
            }

            /*
             * Pages exist but nothing readable was extracted.
             */
            if (finalText.isBlank()) {
                throw new PdfExtractionException(
                        PdfExtractionErrorCode.PDF_NO_EXTRACTABLE_TEXT
                );
            }

            return new PdfExtractionResult(
                    List.copyOf(pages),
                    finalText
            );

        } catch (PdfExtractionException e) {

            throw e;

        } catch (IOException | RuntimeException e) {

            throw new PdfExtractionException(
                    PdfExtractionErrorCode.PDF_EXTRACTION_FAILED
            );
        }
    }

    private String normalizeText(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        return text
                // Normalize Windows/Mac line endings
                .replace("\r\n", "\n")
                .replace('\r', '\n')

                // Multiple spaces/tabs -> one space
                .replaceAll("[\\t ]+", " ")

                // Remove spaces around newline
                .replaceAll(" *\\n *", "\n")

                // Keep paragraph breaks but remove excessive blanks
                .replaceAll("\\n{3,}", "\n\n")

                .trim();
    }

    private boolean containsImage(
            PDResources resources,
            int depth
    ) throws IOException {

        if (resources == null || depth > 5) {
            return false;
        }

        for (COSName name : resources.getXObjectNames()) {

            PDXObject object = resources.getXObject(name);

            if (object instanceof PDImageXObject) {
                return true;
            }

            if (object instanceof PDFormXObject form) {

                if (containsImage(
                        form.getResources(),
                        depth + 1
                )) {
                    return true;
                }
            }
        }

        return false;
    }
}
