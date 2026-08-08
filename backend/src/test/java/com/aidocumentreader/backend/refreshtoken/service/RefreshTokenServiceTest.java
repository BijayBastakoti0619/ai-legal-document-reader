package com.aidocumentreader.backend.refreshtoken.service;

import com.aidocumentreader.backend.refreshtoken.entity.RefreshToken;
import com.aidocumentreader.backend.refreshtoken.repository.RefreshTokenRepository;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldCreateAndReturnRawRefreshTokenWhileSavingHash() {
        // Arrange: Mock the database returning a user
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act: Generate the token
        String rawToken = refreshTokenService.createRefreshToken(userId);

        // Assert 1: The service must give us back a usable token string
        assertThat(rawToken).isNotBlank();

        // Assert 2: Capture the exact Entity that the service tried to save to the database
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        // Assert 3: Prove that the database got the hashed version, NOT the raw token
        assertThat(savedToken.getUser()).isEqualTo(mockUser);
        assertThat(savedToken.getTokenHash()).isNotBlank();
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(savedToken.getExpiresAt()).isNotNull();
    }
}