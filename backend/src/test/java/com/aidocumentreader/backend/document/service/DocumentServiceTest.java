package com.aidocumentreader.backend.document.service;

import com.aidocumentreader.backend.document.dto.DocumentDetailResponse;
import com.aidocumentreader.backend.document.dto.DocumentSummaryResponse;
import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.entity.DocumentType; // FIX: Added import
import com.aidocumentreader.backend.document.repository.DocumentRepository;
import com.aidocumentreader.backend.document.validation.PdfValidationService;
import com.aidocumentreader.backend.storage.service.B2StorageService;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
    void shouldReturnPaginatedDocumentSummaries() {
        // Arrange: Mock the user lookup
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userService.getCurrentUser(email)).thenReturn(user);

        // Arrange: Mock the paginated database response
        Pageable pageable = PageRequest.of(0, 10);
        Document doc = new Document();
        doc.setId(100L);
        doc.setOriginalFilename("test.pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(5000L);
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setDocumentType(DocumentType.valueOf("LEASE")); // FIX: Add document type to prevent NPE during mapping

        Page<Document> mockPage = new PageImpl<>(List.of(doc), pageable, 1);

        // FIX: Stub the derived query method call
        when(documentRepository.findAllByUserIdAndStatusNotOrderByCreatedAtDesc(eq(1L), eq(DocumentStatus.DELETED), eq(pageable)))
                .thenReturn(mockPage);

        // Act
        Page<DocumentSummaryResponse> result = documentService.getDocuments(pageable, email);

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).originalFilename()).isEqualTo("test.pdf");
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
    }

    @Test
    void shouldReturnDocumentDetailWhenAuthorized() {
        // Arrange
        String email = "owner@example.com";
        User user = new User();
        user.setId(5L);
        user.setEmail(email);

        when(userService.getCurrentUser(email)).thenReturn(user);

        Long documentId = 15L;
        Document doc = new Document();
        doc.setId(documentId);
        doc.setOriginalFilename("lease.pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setDocumentType(DocumentType.valueOf("LEASE")); // FIX: Add document type to prevent NPE during mapping

        // FIX: Updated mock call to include the DELETED status parameter
        when(documentRepository.findByIdAndUserIdAndStatusNot(documentId, 5L, DocumentStatus.DELETED)).thenReturn(Optional.of(doc));

        // Act
        DocumentDetailResponse result = documentService.getDocument(documentId, email);

        // Assert
        assertThat(result.id()).isEqualTo(documentId);
        assertThat(result.originalFilename()).isEqualTo("lease.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
    }

    @Test
    void shouldThrowExceptionWhenDocumentNotFoundOrUnauthorized() {
        // Arrange: Simulating User B trying to access User A's document
        String email = "hacker@example.com";
        User user = new User();
        user.setId(99L);
        user.setEmail(email);

        when(userService.getCurrentUser(email)).thenReturn(user);

        Long documentId = 15L;
        // FIX: Updated mock call to include the DELETED status parameter
        when(documentRepository.findByIdAndUserIdAndStatusNot(documentId, 99L, DocumentStatus.DELETED)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> documentService.getDocument(documentId, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Document not found");
    }
}