CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(320) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       display_name VARCHAR(100) NOT NULL,
                       role VARCHAR(30) NOT NULL DEFAULT 'USER',
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'))
);

CREATE INDEX idx_users_email ON users(email);