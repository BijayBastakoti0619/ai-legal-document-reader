package com.aidocumentreader.backend.document.controller;

import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.document.service.DocumentService;
import com.aidocumentreader.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DocumentControllerTest {

    private DocumentService documentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        documentService =
                mock(DocumentService.class);

        DocumentController controller =
                new DocumentController(
                        documentService
                );

        mockMvc =
                standaloneSetup(controller)
                        .build();
    }

    @Test
    void uploadDocument_shouldUseAuthenticatedPrincipalEmail()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "contract.pdf",
                        "application/pdf",
                        "%PDF-test"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        User user = new User();
        user.setId(42L);

        Document document = new Document();
        document.setUser(user);
        document.setOriginalFilename(
                "contract.pdf"
        );
        document.setContentType(
                "application/pdf"
        );
        document.setFileSize(
                file.getSize()
        );
        document.setStatus(
                DocumentStatus.UPLOADED
        );

        when(
                documentService.uploadDocument(
                        eq(file),
                        eq("owner@example.com")
                )
        ).thenReturn(document);

        Principal principal =
                () -> "owner@example.com";

        mockMvc.perform(
                        multipart(
                                "/api/v1/documents"
                        )
                                .file(file)
                                .principal(principal)
                )
                .andExpect(
                        status().isCreated()
                );

        verify(documentService)
                .uploadDocument(
                        eq(file),
                        eq("owner@example.com")
                );
    }

    @Test
    void uploadDocument_shouldIgnoreClientSuppliedUserIdAndUsePrincipal()
            throws Exception {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "contract.pdf",
                        "application/pdf",
                        "%PDF-test"
                                .getBytes(
                                        StandardCharsets.UTF_8
                                )
                );

        User user = new User();
        user.setId(42L);

        Document document =
                new Document();

        document.setUser(user);
        document.setOriginalFilename(
                "contract.pdf"
        );
        document.setContentType(
                "application/pdf"
        );
        document.setFileSize(
                file.getSize()
        );
        document.setStatus(
                DocumentStatus.UPLOADED
        );

        when(
                documentService.uploadDocument(
                        any(),
                        eq("owner@example.com")
                )
        ).thenReturn(document);

        Principal principal =
                () -> "owner@example.com";

        mockMvc.perform(
                        multipart(
                                "/api/v1/documents"
                        )
                                .file(file)

                                /*
                                 * Malicious/irrelevant attempt:
                                 */
                                .param(
                                        "userId",
                                        "999"
                                )

                                .principal(principal)
                )
                .andExpect(
                        status().isCreated()
                );

        /*
         * Service still receives the authenticated
         * principal email.
         *
         * It never receives userId=999.
         */
        verify(documentService)
                .uploadDocument(
                        any(),
                        eq("owner@example.com")
                );
    }
}
