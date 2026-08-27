package com.aidocumentreader.backend.document.repository;

import com.aidocumentreader.backend.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // For the paginated history table - strictly bound to the user's ID
    Page<Document> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // For the details page - ensures the document belongs to the requesting user
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}