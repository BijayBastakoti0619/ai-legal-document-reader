package com.aidocumentreader.backend.refreshtoken.repository;

import com.aidocumentreader.backend.TestcontainersConfiguration;
import com.aidocumentreader.backend.refreshtoken.entity.RefreshToken;
import com.aidocumentreader.backend.user.entity.Role;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class RefreshTokenRepositoryTest {

    // We will build this next
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // UserRepository to satisfy the database foreign key constraints!
    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindRefreshTokenByTokenHash() {
        // Arrange 1: Create and save a valid User[cite: 8]
        User user = new User();
        user.setEmail("session-test@example.com");
        user.setPasswordHash("dummy-hash");
        user.setDisplayName("Session User");
        user.setRole(Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        // Arrange 2: Create and save a RefreshToken
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash("mock-sha256-hash-string");
        token.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS)); // Expires in 7 days
        refreshTokenRepository.save(token);

        // Act: Try to fetch the token back out using the exact hash
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByTokenHash("mock-sha256-hash-string");

        // Assert
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUser().getEmail()).isEqualTo("session-test@example.com");
    }
}