package com.aidocumentreader.backend.auth.service;

import com.aidocumentreader.backend.auth.dto.LoginRequest;
import com.aidocumentreader.backend.auth.dto.LoginResponse;
import com.aidocumentreader.backend.auth.dto.RefreshRequest;
import com.aidocumentreader.backend.auth.dto.RefreshResponse;
import com.aidocumentreader.backend.auth.jwttoken.JwtService;
import com.aidocumentreader.backend.refreshtoken.service.RefreshTokenService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldThrowExceptionWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest("wrong@example.com", "password");
        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        User user = new User();
        user.setPasswordHash("hashed-password");

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void shouldReturnLoginResponseWithRealTokensWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@example.com", "CorrectPassword123!");
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        user.setDisplayName("Test User");
        user.setRole(Role.USER);

        when(userRepository.findByEmailIgnoreCase(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);

        when(jwtService.generateAccessToken(user.getEmail())).thenReturn("real-jwt-token");
        when(refreshTokenService.createRefreshToken(user.getId())).thenReturn("real-refresh-token");

        LoginResponse response = loginService.login(request);

        assertThat(response.accessToken()).isEqualTo("real-jwt-token");
        assertThat(response.refreshToken()).isEqualTo("real-refresh-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
    }

    // --- NEW TESTS FOR REFRESH AND LOGOUT ---

    @Test
    void shouldRotateTokensSuccessfully() {
        // Arrange
        RefreshRequest request = new RefreshRequest("old-refresh-token");
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        // Mock the expected behavior of the RefreshTokenService (which we will build next)
        when(refreshTokenService.verifyAndRevokeToken(request.refreshToken())).thenReturn(mockUser);
        when(refreshTokenService.createRefreshToken(mockUser.getId())).thenReturn("new-refresh-token");
        when(jwtService.generateAccessToken(mockUser.getEmail())).thenReturn("new-jwt-token");

        // Act
        RefreshResponse response = loginService.refreshToken(request);

        // Assert
        assertThat(response.accessToken()).isEqualTo("new-jwt-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldRevokeTokenOnLogout() {
        // Arrange
        RefreshRequest request = new RefreshRequest("token-to-revoke");

        // Act
        loginService.logout(request);

        // Assert
        // Verify that the LoginService successfully tells the RefreshTokenService to kill the token
        verify(refreshTokenService).verifyAndRevokeToken(request.refreshToken());
    }
}