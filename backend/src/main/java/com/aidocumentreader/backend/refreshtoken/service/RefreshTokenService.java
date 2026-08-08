package com.aidocumentreader.backend.refreshtoken.service;

import com.aidocumentreader.backend.refreshtoken.entity.RefreshToken;
import com.aidocumentreader.backend.refreshtoken.repository.RefreshTokenRepository;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createRefreshToken(Long userId) {
        // 1. Fetch the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Generate a secure random raw token
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // 3. Hash the raw token using SHA-256
        String tokenHash = hashToken(rawToken);

        // 4. Create and save the entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS)); // 7 days expiry

        refreshTokenRepository.save(refreshToken);

        // 5. Return the raw token to the user
        return rawToken;
    }

    // Helper method to perform the SHA-256 one-way hashing
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash refresh token", e);
        }
    }

    // --- NEW SKELETON METHOD ---

    @Transactional
    public User verifyAndRevokeToken(String rawToken) {
        // 1. Hash the raw token provided by the user
        String tokenHash = hashToken(rawToken);

        // 2. Look it up in the database
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // 3. Security Check: Is it already revoked? (Prevents replay attacks)
        if (token.getRevokedAt() != null) {
            throw new IllegalArgumentException("Refresh token has already been revoked");
        }

        // 4. Security Check: Is it expired?
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // 5. Throw the Kill Switch
        token.setRevokedAt(Instant.now());
        refreshTokenRepository.save(token);

        // 6. Return the authenticated user
        return token.getUser();
    }
}