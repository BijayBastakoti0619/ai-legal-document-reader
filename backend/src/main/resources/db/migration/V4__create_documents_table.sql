CREATE TABLE documents (
                           id BIGSERIAL PRIMARY KEY,

                           user_id BIGINT NOT NULL,

                           original_filename VARCHAR(255) NOT NULL,
                           storage_key VARCHAR(1024) NOT NULL,
                           content_type VARCHAR(100) NOT NULL,
                           file_size BIGINT NOT NULL,
                           sha256 VARCHAR(64) NOT NULL,

                           status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED',

                           created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_documents_user
                               FOREIGN KEY (user_id)
                                   REFERENCES users(id),

                           CONSTRAINT uk_documents_storage_key
                               UNIQUE (storage_key),

                           CONSTRAINT chk_documents_file_size
                               CHECK (file_size > 0),

                           CONSTRAINT chk_documents_sha256
                               CHECK (CHAR_LENGTH(sha256) = 64),

                           CONSTRAINT chk_documents_status
                               CHECK (
                                   status IN (
                                              'UPLOADED',
                                              'EXTRACTING',
                                              'ANALYZING',
                                              'COMPLETED',
                                              'FAILED',
                                              'DELETED'
                                       )
                                   )
);

CREATE INDEX idx_documents_user_created_at
    ON documents(user_id, created_at DESC);