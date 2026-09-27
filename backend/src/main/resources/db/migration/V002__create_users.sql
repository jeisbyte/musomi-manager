-- V002__create_users.sql
-- Creates the users table.

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_users_school FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT uq_users_school_username UNIQUE (school_id, username),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT'))
);

CREATE INDEX idx_users_school_id ON users (school_id);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_school_active ON users (school_id) WHERE is_active = TRUE;
