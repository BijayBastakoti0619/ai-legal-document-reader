package com.aidocumentreader.backend.auth.controller;

import com.aidocumentreader.backend.auth.service.LoginService;
import com.aidocumentreader.backend.auth.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    @Test
    void shouldReturn400WhenLoginRequestIsInvalid() throws Exception {
        // Arrange: A JSON payload with a blank password and invalid email
        String invalidJsonPayload = """
                {
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        // Act & Assert: Send POST request and expect 400 Bad Request due to validation
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenLoginRequestIsValid() throws Exception {
        // Arrange: A valid JSON payload
        String validJsonPayload = """
                {
                  "email": "user@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        // Act & Assert: Send POST request and expect 200 OK
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPayload))
                .andExpect(status().isOk());
    }
}