package com.aidocumentreader.backend.document.repository;

import com.aidocumentreader.backend.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
