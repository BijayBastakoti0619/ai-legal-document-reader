package com.aidocumentreader.backend.document.service;

import com.aidocumentreader.backend.document.dto.DocumentDetailResponse;
import com.aidocumentreader.backend.document.dto.DocumentSummaryResponse;
import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.repository.DocumentRepository;
import com.aidocumentreader.backend.document.validation.PdfValidationService;
import com.aidocumentreader.backend.exception.DocumentNotFoundException;
import com.aidocumentreader.backend.exception.DocumentUploadException;
import com.aidocumentreader.backend.storage.service.B2StorageService;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final PdfValidationService pdfValidationService;
    private final Sha256Service sha256Service;
    private final B2StorageService b2StorageService;
    private final UserService userService;

    public DocumentService(
            DocumentRepository documentRepository,
            PdfValidationService pdfValidationService,
            Sha256Service sha256Service,
            B2StorageService b2StorageService,
            UserService userService
    ) {
        this.documentRepository = documentRepository;
        this.pdfValidationService = pdfValidationService;
        this.sha256Service = sha256Service;
        this.b2StorageService = b2StorageService;
        this.userService = userService;
    }

    public Document uploadDocument(
            MultipartFile file,
            String authenticatedEmail
    ) {

        pdfValidationService.validate(file);
        User user = userService.getCurrentUser(authenticatedEmail);
        byte[] fileBytes = readFileBytes(file);
        String sha256 = sha256Service.calculate(fileBytes);
        String objectKey = createObjectKey(user.getId());
        String originalFilename = getSafeOriginalFilename(file);
        String contentType = file.getContentType();

        try {
            b2StorageService.upload(
                    objectKey,
                    fileBytes,
                    contentType
            );

        } catch (RuntimeException exception) {
            throw new DocumentUploadException(
                    "The document could not be uploaded.",
                    exception
            );
        }

        Document document = new Document();
        document.setUser(user);
        document.setOriginalFilename(originalFilename);
        document.setStorageKey(objectKey);
        document.setContentType(contentType);
        document.setFileSize(file.getSize());
        document.setSha256(sha256);
        document.setStatus(DocumentStatus.UPLOADED);

        try {
            return documentRepository.saveAndFlush(document);

        } catch (RuntimeException exception) {
            cleanupUploadedObject(objectKey);
            throw new DocumentUploadException(
                    "The document could not be uploaded.",
                    exception
            );
        }
    }

    public Page<DocumentSummaryResponse> getDocuments(Pageable pageable, String authenticatedEmail) {
        User user = userService.getCurrentUser(authenticatedEmail);

        Page<Document> documents = documentRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        // Map the Entity to the immutable Record DTO
        return documents.map(doc -> new DocumentSummaryResponse(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getFileSize(),
                doc.getStatus().name(),
                doc.getCreatedAt()
        ));
    }

    public DocumentDetailResponse getDocument(Long id, String authenticatedEmail) {
        User user = userService.getCurrentUser(authenticatedEmail);

        Document doc = documentRepository.findByIdAndUserId(id, user.getId())
                // FIX: Now throwing a 404 exception instead of a 500 RuntimeException
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        // Map the Entity to the immutable Record DTO
        return new DocumentDetailResponse(
                doc.getId(),
                doc.getOriginalFilename(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getStatus().name(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new DocumentUploadException(
                    "The document could not be read.",
                    exception
            );
        }
    }

    private String createObjectKey(Long userId) {
        return "users/" + userId + "/" + UUID.randomUUID() + ".pdf";
    }

    private String getSafeOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String cleanedFilename = StringUtils.cleanPath(originalFilename);
        return StringUtils.getFilename(cleanedFilename);
    }

    private void cleanupUploadedObject(String objectKey) {
        try {
            b2StorageService.delete(objectKey);
        } catch (RuntimeException cleanupException) {
            log.error(
                    "Failed to remove orphaned B2 object: {}",
                    objectKey,
                    cleanupException
            );
        }
    }
}