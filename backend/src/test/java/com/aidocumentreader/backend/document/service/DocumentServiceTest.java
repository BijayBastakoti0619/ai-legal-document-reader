package com.aidocumentreader.backend.document.service;



import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.repository.DocumentRepository;
import com.aidocumentreader.backend.document.validation.PdfValidationService;
import com.aidocumentreader.backend.exception.DocumentUploadException;
import com.aidocumentreader.backend.storage.service.B2StorageService;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private PdfValidationService pdfValidationService;

    @Mock
    private Sha256Service sha256Service;

    @Mock
    private B2StorageService b2StorageService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DocumentService documentService;


    @Test
    void uploadDocument_shouldUploadAndSaveSuccessfully() {

        String email = "user@example.com";

        byte[] bytes =
                "%PDF-1.7\nTest PDF"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("contract.pdf", bytes);

        User user = createUser(42L);

        String hash =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                        + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

        when(userService.getCurrentUser(email))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(hash);

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Document result =
                documentService.uploadDocument(file, email);

        assertNotNull(result);

        verify(pdfValidationService)
                .validate(file);

        verify(userService)
                .getCurrentUser(email);

        verify(sha256Service)
                .calculate(bytes);

        verify(b2StorageService)
                .upload(
                        anyString(),
                        eq(bytes),
                        eq("application/pdf")
                );

        verify(documentRepository)
                .saveAndFlush(any(Document.class));
    }


    @Test
    void uploadDocument_shouldUseAuthenticatedUserAsOwner() {

        String email = "owner@example.com";

        byte[] bytes =
                "%PDF-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("legal.pdf", bytes);

        User authenticatedUser =
                createUser(99L);

        when(userService.getCurrentUser(email))
                .thenReturn(authenticatedUser);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        documentService.uploadDocument(file, email);

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository)
                .saveAndFlush(captor.capture());

        Document saved =
                captor.getValue();

        assertSame(
                authenticatedUser,
                saved.getUser()
        );

        assertEquals(
                99L,
                saved.getUser().getId()
        );

        assertTrue(
                saved.getStorageKey()
                        .startsWith("users/99/")
        );
    }


    @Test
    void uploadDocument_shouldGenerateValidUuidObjectKey() {

        byte[] bytes =
                "%PDF-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf(
                        "My Secret Contract.pdf",
                        bytes
                );

        User user =
                createUser(55L);

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        documentService.uploadDocument(
                file,
                "user@example.com"
        );

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository)
                .saveAndFlush(captor.capture());

        String storageKey =
                captor.getValue().getStorageKey();

        assertTrue(
                storageKey.startsWith("users/55/")
        );

        assertTrue(
                storageKey.endsWith(".pdf")
        );

        String uuidPart =
                storageKey
                        .substring("users/55/".length())
                        .replace(".pdf", "");

        assertDoesNotThrow(
                () -> UUID.fromString(uuidPart)
        );

        /*
         * Original user-controlled filename must NOT
         * become the B2 object name.
         */
        assertFalse(
                storageKey.contains(
                        "My Secret Contract"
                )
        );
    }


    @Test
    void uploadDocument_shouldSaveCorrectMetadata() {

        byte[] bytes =
                "%PDF-metadata-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf(
                        "agreement.pdf",
                        bytes
                );

        User user =
                createUser(10L);

        String hash =
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
                        + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(hash);

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        documentService.uploadDocument(
                file,
                "user@example.com"
        );

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentRepository)
                .saveAndFlush(captor.capture());

        Document saved =
                captor.getValue();

        assertSame(user, saved.getUser());

        assertEquals(
                "agreement.pdf",
                saved.getOriginalFilename()
        );

        assertEquals(
                "application/pdf",
                saved.getContentType()
        );

        assertEquals(
                bytes.length,
                saved.getFileSize()
        );

        assertEquals(
                hash,
                saved.getSha256()
        );

        assertEquals(
                DocumentStatus.UPLOADED,
                saved.getStatus()
        );

        assertNotNull(
                saved.getStorageKey()
        );
    }


    @Test
    void uploadDocument_shouldUseActualFileBytesForSha256() {

        byte[] bytes =
                "%PDF-exact-content"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("document.pdf", bytes);

        User user =
                createUser(1L);

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        documentService.uploadDocument(
                file,
                "user@example.com"
        );

        verify(sha256Service)
                .calculate(bytes);
    }


    @Test
    void uploadDocument_shouldNotSaveMetadataWhenB2UploadFails() {

        byte[] bytes =
                "%PDF-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("contract.pdf", bytes);

        User user =
                createUser(42L);

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        doThrow(
                new RuntimeException(
                        "B2 unavailable"
                )
        )
                .when(b2StorageService)
                .upload(
                        anyString(),
                        any(byte[].class),
                        eq("application/pdf")
                );

        assertThrows(
                DocumentUploadException.class,
                () -> documentService.uploadDocument(
                        file,
                        "user@example.com"
                )
        );

        verify(
                documentRepository,
                never()
        ).saveAndFlush(any(Document.class));

        verify(
                b2StorageService,
                never()
        ).delete(anyString());
    }


    @Test
    void uploadDocument_shouldDeleteUploadedObjectWhenDatabaseSaveFails() {

        byte[] bytes =
                "%PDF-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("contract.pdf", bytes);

        User user =
                createUser(42L);

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenThrow(
                        new RuntimeException(
                                "Database unavailable"
                        )
                );

        assertThrows(
                DocumentUploadException.class,
                () -> documentService.uploadDocument(
                        file,
                        "user@example.com"
                )
        );

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(b2StorageService)
                .upload(
                        keyCaptor.capture(),
                        eq(bytes),
                        eq("application/pdf")
                );

        String uploadedKey =
                keyCaptor.getValue();

        verify(b2StorageService)
                .delete(uploadedKey);
    }


    @Test
    void uploadDocument_shouldStillThrowUploadExceptionWhenCleanupAlsoFails() {

        byte[] bytes =
                "%PDF-test"
                        .getBytes(StandardCharsets.UTF_8);

        MockMultipartFile file =
                createPdf("contract.pdf", bytes);

        User user =
                createUser(42L);

        when(userService.getCurrentUser(anyString()))
                .thenReturn(user);

        when(sha256Service.calculate(bytes))
                .thenReturn(validHash());

        when(documentRepository.saveAndFlush(any(Document.class)))
                .thenThrow(
                        new RuntimeException(
                                "Database unavailable"
                        )
                );

        doThrow(
                new RuntimeException(
                        "B2 cleanup failed"
                )
        )
                .when(b2StorageService)
                .delete(anyString());

        DocumentUploadException exception =
                assertThrows(
                        DocumentUploadException.class,
                        () -> documentService.uploadDocument(
                                file,
                                "user@example.com"
                        )
                );

        assertEquals(
                "The document could not be uploaded.",
                exception.getMessage()
        );

        verify(b2StorageService)
                .delete(anyString());
    }


    private MockMultipartFile createPdf(
            String filename,
            byte[] bytes
    ) {

        return new MockMultipartFile(
                "file",
                filename,
                "application/pdf",
                bytes
        );
    }


    private User createUser(Long id) {

        User user = new User();
        user.setId(id);

        return user;
    }


    private String validHash() {

        return "cccccccccccccccccccccccccccccccc"
                + "cccccccccccccccccccccccccccccccc";
    }
}