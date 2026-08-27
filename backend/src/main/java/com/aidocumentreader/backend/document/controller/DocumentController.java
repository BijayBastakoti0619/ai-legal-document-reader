package com.aidocumentreader.backend.document.controller;

import com.aidocumentreader.backend.document.dto.DocumentContent;
import com.aidocumentreader.backend.document.dto.DocumentSummaryResponse;
import com.aidocumentreader.backend.document.dto.DocumentUploadResponse;
import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentType;
import com.aidocumentreader.backend.document.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentUploadResponse uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType,
            Principal principal
    ) {
        String authenticatedEmail = principal.getName();

        Document document =
                documentService.uploadDocument(
                        file,
                        authenticatedEmail,
                        documentType
                );

        return new DocumentUploadResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getDocumentType(),
                document.getStatus(),
                document.getCreatedAt()
        );

    }

    @GetMapping("/{documentId}/content")
    public ResponseEntity<byte[]> getDocumentContent(
            @PathVariable Long documentId,
            Principal principal
    ) {

        String authenticatedEmail =
                principal.getName();

        DocumentContent content =
                documentService.getDocumentContent(
                        documentId,
                        authenticatedEmail
                );

        ContentDisposition disposition =
                ContentDisposition.inline()
                        .filename(
                                content.filename(),
                                StandardCharsets.UTF_8
                        )
                        .build();

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                content.contentType()
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .contentLength(
                        content.bytes().length
                )
                .body(
                        content.bytes()
                );
    }

    @GetMapping
    public Page<DocumentSummaryResponse> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Pageable pageable = PageRequest.of(page, size);

        // Pass the pagination request and the secure email from the JWT
        return documentService.getDocuments(pageable, principal.getName());
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(
            @PathVariable Long documentId,
            Principal principal
    ) {

        String authenticatedEmail =
                principal.getName();

        documentService.deleteDocument(
                documentId,
                authenticatedEmail
        );
    }
}

