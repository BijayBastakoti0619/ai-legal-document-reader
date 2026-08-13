package com.aidocumentreader.backend.auth.controller;

import com.aidocumentreader.backend.auth.jwttoken.JwtAuthenticationFilter;
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
@AutoConfigureMockMvc(addFilters = false) // Bypasses the security filters for the web test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    // --- ADDED THIS TO SATISFY SECURITY CONFIGURATIONS ---
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturn400WhenLoginRequestIsInvalid() throws Exception {
        String invalidJsonPayload = """
                {
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJsonPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenLoginRequestIsValid() throws Exception {
        String validJsonPayload = """
                {
                  "email": "user@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenRefreshRequestIsValid() throws Exception {
        String validJsonPayload = """
                {
                  "refreshToken": "some-valid-refresh-token-hash"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPayload))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200WhenLogoutRequestIsValid() throws Exception {
        String validJsonPayload = """
                {
                  "refreshToken": "some-valid-refresh-token-hash"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPayload))
                .andExpect(status().isOk());
    }
}