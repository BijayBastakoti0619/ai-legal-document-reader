package com.aidocumentreader.backend.user.controller;

import com.aidocumentreader.backend.auth.jwttoken.JwtAuthenticationFilter;
import com.aidocumentreader.backend.user.dto.UserProfileResponse;
import com.aidocumentreader.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters for the web slice test
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldReturn200AndUserProfileWhenRequested() throws Exception {
        // Arrange
        UserProfileResponse mockProfile = new UserProfileResponse(
                1L,
                "user@example.com",
                "Sample User",
                "USER"
        );

        // Note: In a real request, the email comes from the SecurityContext.
        // For this web layer test, we mock the service response directly.
        when(userService.getCurrentUserProfile("user@example.com")).thenReturn(mockProfile);

        // Act & Assert
        // We will simulate the Principal injection in the actual controller code next
        mockMvc.perform(get("/api/v1/users/me")
                        .principal(() -> "user@example.com")) // Simulates the authenticated user's email
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("Sample User"));
    }
}