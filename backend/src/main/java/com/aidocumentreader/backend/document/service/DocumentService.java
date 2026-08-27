package com.aidocumentreader.backend.document.service;

import com.aidocumentreader.backend.document.dto.DocumentContent;
import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.entity.DocumentType;
import com.aidocumentreader.backend.document.repository.DocumentRepository;
import com.aidocumentreader.backend.document.validation.PdfValidationService;
import com.aidocumentreader.backend.exception.DocumentNotFoundException;
import com.aidocumentreader.backend.exception.DocumentUploadException;
import com.aidocumentreader.backend.storage.service.B2StorageService;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentService.class);

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
            String authenticatedEmail,
            DocumentType documentType
    ) {

        pdfValidationService.validate(file);

        User user =
                userService.getCurrentUser(authenticatedEmail);

        byte[] fileBytes = readFileBytes(file);

        String sha256 =
                sha256Service.calculate(fileBytes);

        String objectKey =
                createObjectKey(user.getId());

        String originalFilename =
                getSafeOriginalFilename(file);

        String contentType =
                file.getContentType();

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
        document.setDocumentType(documentType);
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

        return "users/"
                + userId
                + "/"
                + UUID.randomUUID()
                + ".pdf";
    }

    private String getSafeOriginalFilename(
            MultipartFile file
    ) {

        String originalFilename =
                file.getOriginalFilename();

        String cleanedFilename =
                StringUtils.cleanPath(originalFilename);

        return StringUtils.getFilename(cleanedFilename);
    }

    private void cleanupUploadedObject(
            String objectKey
    ) {

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

    public DocumentContent getDocumentContent(
            Long documentId,
            String authenticatedEmail
    ) {

        User authenticatedUser =
                userService.getCurrentUser(
                        authenticatedEmail
                );

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new DocumentNotFoundException(
                                        "Document not found."
                                )
                        );

        /*
         * Security check:
         * the authenticated user must own the document.
         */
        if (!document.getUser()
                .getId()
                .equals(authenticatedUser.getId())) {

            /*
             * We intentionally say "not found"
             * instead of revealing that another
             * user's document exists.
             */
            throw new DocumentNotFoundException(
                    "Document not found."
            );
        }

        byte[] fileBytes;

        try {

            fileBytes =
                    b2StorageService.download(
                            document.getStorageKey()
                    );

        } catch (RuntimeException exception) {

            throw new DocumentUploadException(
                    "The document could not be retrieved.",
                    exception
            );
        }

        return new DocumentContent(
                document.getOriginalFilename(),
                document.getContentType(),
                fileBytes
        );
    }

    public void deleteDocument(
            Long documentId,
            String authenticatedEmail
    ) {

        User authenticatedUser =
                userService.getCurrentUser(
                        authenticatedEmail
                );

        Document document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new DocumentNotFoundException(
                                        "Document not found."
                                )
                        );

        if (!document.getUser()
                .getId()
                .equals(authenticatedUser.getId())) {

            throw new DocumentNotFoundException(
                    "Document not found."
            );
        }

        if (document.getStatus()
                == DocumentStatus.DELETED) {

            throw new DocumentNotFoundException(
                    "Document not found."
            );
        }

        b2StorageService.delete(
                document.getStorageKey()
        );

        document.setStatus(
                DocumentStatus.DELETED
        );

        documentRepository.saveAndFlush(
                document
        );
    }
}