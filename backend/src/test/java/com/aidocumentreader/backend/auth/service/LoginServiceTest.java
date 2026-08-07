package com.aidocumentreader.backend.auth.service;

import com.aidocumentreader.backend.auth.dto.LoginRequest;
import com.aidocumentreader.backend.auth.dto.LoginResponse;
import com.aidocumentreader.backend.user.entity.Role;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist() {
        // Arrange
        LoginRequest request = new LoginRequest("unknown@example.com", "StrongPassword123!");

        // Tell Mockito to return an empty Optional when the repository is called
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "WrongPassword!");

        User mockUser = new User();
        mockUser.setPasswordHash("database-hashed-password");

        // Mock the repository to return our fake user, but mock the encoder to return FALSE
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldReturnLoginResponseWhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "StrongPassword123!");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");
        mockUser.setDisplayName("Test User");
        mockUser.setRole(Role.USER);
        mockUser.setPasswordHash("database-hashed-password");

        // Mock the repository to return the user, and the encoder to return TRUE
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPasswordHash())).thenReturn(true);

        // Act
        LoginResponse response = loginService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("temp-jwt-access-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        assertThat(response.user().role()).isEqualTo("USER");
    }
}