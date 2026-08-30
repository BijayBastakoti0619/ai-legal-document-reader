package com.aidocumentreader.backend.document.repository;

import com.aidocumentreader.backend.TestcontainersConfiguration;
import com.aidocumentreader.backend.document.entity.Document;
import com.aidocumentreader.backend.document.entity.DocumentStatus;
import com.aidocumentreader.backend.user.entity.Role;
import com.aidocumentreader.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class DocumentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void shouldFindAllByUserIdOrderByCreatedAtDesc() {
        // Arrange: Create User A and User B
        User userA = new User();
        userA.setEmail("usera@example.com");
        userA.setPasswordHash("dummy_hash_a");
        userA.setDisplayName("User A");
        userA.setRole(Role.valueOf("USER"));
        userA = entityManager.persistAndFlush(userA);

        User userB = new User();
        userB.setEmail("userb@example.com");
        userB.setPasswordHash("dummy_hash_b");
        userB.setDisplayName("User B");
        userB.setRole(Role.valueOf("USER"));
        userB = entityManager.persistAndFlush(userB);

        // Arrange: Create Documents for User A
        Document doc1 = new Document();
        doc1.setUser(userA);
        doc1.setOriginalFilename("lease.pdf");
        doc1.setContentType("application/pdf");
        doc1.setFileSize(1024L);
        doc1.setSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        doc1.setStatus(DocumentStatus.valueOf("UPLOADED"));
        doc1.setStorageKey("storage-key-1");
        entityManager.persist(doc1);

        Document doc2 = new Document();
        doc2.setUser(userA);
        doc2.setOriginalFilename("contract.pdf");
        doc2.setContentType("application/pdf");
        doc2.setFileSize(2048L);
        doc2.setSha256("a3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        doc2.setStatus(DocumentStatus.valueOf("COMPLETED"));
        doc2.setStorageKey("storage-key-2");
        entityManager.persist(doc2);

        // Arrange: Create Document for User B
        Document doc3 = new Document();
        doc3.setUser(userB);
        doc3.setOriginalFilename("other.pdf");
        doc3.setContentType("application/pdf");
        doc3.setFileSize(500L);
        doc3.setSha256("b3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        doc3.setStatus(DocumentStatus.valueOf("UPLOADED"));
        doc3.setStorageKey("storage-key-3");
        entityManager.persistAndFlush(doc3);

        // Act: Fetch User A's documents using the NEW derived query method
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> result = documentRepository.findAllByUserIdAndStatusNotOrderByCreatedAtDesc(userA.getId(), DocumentStatus.DELETED, pageable);

        // Assert: Verify User B's document is excluded
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(Document::getOriginalFilename)
                .containsExactlyInAnyOrder("contract.pdf", "lease.pdf");
    }

    // FIX: Updated method name and added DocumentStatus to assertions
    @Test
    void shouldFindByIdAndUserIdAndStatusNot() {
        // Arrange
        User user = new User();
        user.setEmail("owner@example.com");
        user.setPasswordHash("dummy_hash_owner");
        user.setDisplayName("Owner");
        user.setRole(Role.valueOf("USER"));
        user = entityManager.persistAndFlush(user);

        Document doc = new Document();
        doc.setUser(user);
        doc.setOriginalFilename("lease.pdf");
        doc.setContentType("application/pdf");
        doc.setFileSize(1024L);
        doc.setSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        doc.setStatus(DocumentStatus.valueOf("UPLOADED"));
        doc.setStorageKey("storage-key-owner");
        doc = entityManager.persistAndFlush(doc);

        // Act - successful find
        Optional<Document> found = documentRepository.findByIdAndUserIdAndStatusNot(doc.getId(), user.getId(), DocumentStatus.DELETED);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getOriginalFilename()).isEqualTo("lease.pdf");

        // Act - unauthorized find
        Optional<Document> notFound = documentRepository.findByIdAndUserIdAndStatusNot(doc.getId(), 999L, DocumentStatus.DELETED);

        // Assert
        assertThat(notFound).isEmpty();
    }
}