package com.aidocumentreader.backend.document.repository;

import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // INDUSTRY STANDARD: Derived query method automatically filters out the provided status
    Page<Document> findAllByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, DocumentStatus status, Pageable pageable);

    // For the details page - ensures the document belongs to the requesting user
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}