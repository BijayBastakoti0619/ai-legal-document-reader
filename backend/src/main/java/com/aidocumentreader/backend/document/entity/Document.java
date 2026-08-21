package com.aidocumentreader.backend.document.entity;

import com.aidocumentreader.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_documents_storage_key",
                        columnNames = "storage_key"
                )
        }
)
public class Document {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_documents_user")
    )
    private User user;

    @Setter
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Setter
    @Column(name = "storage_key", nullable = false, unique = true, length = 1024)
    private String storageKey;

    @Setter
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Setter
    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Setter
    @Column(nullable = false, length = 64)
    private String sha256;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = DocumentStatus.UPLOADED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getSha256() {
        return sha256;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}