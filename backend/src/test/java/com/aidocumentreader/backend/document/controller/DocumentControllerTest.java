package com.aidocumentreader.backend.document.controller;

import com.aidocumentreader.backend.auth.jwttoken.JwtAuthenticationFilter;
import com.aidocumentreader.backend.auth.service.CustomUserDetailsService;
import com.aidocumentreader.backend.document.dto.DocumentDetailResponse;
import com.aidocumentreader.backend.document.dto.DocumentSummaryResponse;
import com.aidocumentreader.backend.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentService documentService;

    // --- FIX: Mock the security beans that break the WebMvcTest slice ---
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    // A simple mock principal to simulate our authenticated user
    private final Principal mockPrincipal = () -> "user@example.com";

    @Test
    void shouldReturnPaginatedDocuments() throws Exception {
        // Arrange: Added the missing "LEASE" documentType to the constructor
        DocumentSummaryResponse summary = new DocumentSummaryResponse(
                15L, "lease.pdf", 1024L, "LEASE", "UPLOADED", Instant.now()
        );

        when(documentService.getDocuments(any(Pageable.class), eq("user@example.com")))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(15))
                .andExpect(jsonPath("$.content[0].originalFilename").value("lease.pdf"))
                .andExpect(jsonPath("$.content[0].documentType").value("LEASE")); // FIX: Verify category in JSON
    }

    @Test
    void shouldReturnDocumentDetail() throws Exception {
        // Arrange: Added the missing "LEASE" documentType to the constructor
        DocumentDetailResponse detail = new DocumentDetailResponse(
                15L, "lease.pdf", "application/pdf", 1024L, "LEASE", "UPLOADED", Instant.now(), Instant.now()
        );

        when(documentService.getDocument(15L, "user@example.com")).thenReturn(detail);

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents/15")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.originalFilename").value("lease.pdf"))
                .andExpect(jsonPath("$.documentType").value("LEASE")); // FIX: Verify category in JSON
    }
}