-- V025__create_import_jobs.sql
-- Creates the import_jobs table. History of Excel imports.

CREATE TABLE import_jobs (
    id                 BIGSERIAL PRIMARY KEY,
    school_id          BIGINT       NOT NULL,
    job_reference      VARCHAR(50)  NOT NULL,
    import_type        VARCHAR(20)  NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    total_rows         INT          NOT NULL DEFAULT 0,
    valid_rows         INT          NOT NULL DEFAULT 0,
    error_rows         INT          NOT NULL DEFAULT 0,
    errors_json        JSONB,
    original_filename  VARCHAR(255),
    created_by         BIGINT       NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    confirmed_at       TIMESTAMP,

    CONSTRAINT fk_import_jobs_school_id  FOREIGN KEY (school_id)  REFERENCES schools(id),
    CONSTRAINT fk_import_jobs_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uk_import_jobs_reference UNIQUE (job_reference),
    CONSTRAINT ck_import_jobs_type CHECK (import_type IN ('STUDENTS')),
    CONSTRAINT ck_import_jobs_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED'))
);

CREATE INDEX idx_import_jobs_school_id ON import_jobs(school_id);
CREATE INDEX idx_import_jobs_job_reference ON import_jobs(job_reference);