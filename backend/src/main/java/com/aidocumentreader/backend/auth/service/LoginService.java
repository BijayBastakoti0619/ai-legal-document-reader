package com.aidocumentreader.backend.auth.service;

import com.aidocumentreader.backend.auth.dto.LoginRequest;
import com.aidocumentreader.backend.auth.dto.LoginResponse;
import com.aidocumentreader.backend.auth.dto.RefreshRequest;
import com.aidocumentreader.backend.auth.dto.RefreshResponse;
import com.aidocumentreader.backend.auth.jwttoken.JwtService;
import com.aidocumentreader.backend.refreshtoken.service.RefreshTokenService;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                900,
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole().name()
                )
        );
    }

    @Transactional
    public RefreshResponse refreshToken(RefreshRequest request) {
        // 1. Verify and kill the old token
        User user = refreshTokenService.verifyAndRevokeToken(request.refreshToken());

        // 2. Generate a fresh pair of tokens (Rotation)
        String newAccessToken = jwtService.generateAccessToken(user.getEmail());
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        // 3. Return the new secure session to the user
        return new RefreshResponse(newAccessToken, newRefreshToken, "Bearer", 900);
    }

    public void logout(RefreshRequest request) {
        // Safely revoke the token in the database so it can never be used again
        refreshTokenService.verifyAndRevokeToken(request.refreshToken());
    }
}