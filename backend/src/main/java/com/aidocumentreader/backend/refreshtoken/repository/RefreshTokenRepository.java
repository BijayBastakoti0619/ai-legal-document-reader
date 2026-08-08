package com.aidocumentreader.backend.refreshtoken.repository;

import com.aidocumentreader.backend.refreshtoken.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // SELECT * FROM refresh_tokens WHERE token_hash = ?
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}