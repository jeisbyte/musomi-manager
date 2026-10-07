-- V012__create_subjects.sql
-- Creates the subjects table. Subjects per school (Math, English, etc.).
-- Soft-deletable via is_active.

CREATE TABLE subjects (
    id           BIGSERIAL PRIMARY KEY,
    school_id    BIGINT       NOT NULL,
    code         VARCHAR(20)  NOT NULL,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    is_core      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,

    CONSTRAINT fk_subjects_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT uk_subjects_school_code UNIQUE (school_id, code)
);

CREATE INDEX idx_subjects_school_id ON subjects(school_id);
CREATE INDEX idx_subjects_code ON subjects(code);