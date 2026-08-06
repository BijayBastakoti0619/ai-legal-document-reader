package com.aidocumentreader.backend.auth.service;

import com.aidocumentreader.backend.auth.dto.RegisterRequest;
import com.aidocumentreader.backend.auth.dto.RegisterResponse;
import com.aidocumentreader.backend.exception.EmailAlreadyExistsException;
import com.aidocumentreader.backend.user.entity.Role;
import com.aidocumentreader.backend.user.entity.User;
import com.aidocumentreader.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        String normalizedDisplayName = request.displayName().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(normalizedDisplayName);
        user.setRole(Role.USER);
        user.setEnabled(true);

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getDisplayName(),
                    savedUser.getRole()
            );
        } catch (DataIntegrityViolationException exception) {
            /*
             * Handles simultaneous registration attempts.
             * The database unique constraint is the final protection.
             */
            throw new EmailAlreadyExistsException();
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}