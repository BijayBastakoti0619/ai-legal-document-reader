package com.aidocumentreader.backend.auth.service;

import com.aidocumentreader.backend.auth.dto.LoginRequest;
import com.aidocumentreader.backend.auth.dto.LoginResponse;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Injecting the repository and the security encoder via the constructor
    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Fetch the user by email from the database
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 2. Verify the raw password against the hashed password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Return the success response with dummy tokens for now
        return new LoginResponse(
                "temp-jwt-access-token",
                "temp-refresh-token",
                "Bearer",
                900, // 15 minutes in seconds
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole().name()
                )
        );
    }
}