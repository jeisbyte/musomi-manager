-- V003__create_academic_years.sql
-- Creates the academic_years table.
-- Years like 2024, 2025, 2026 per school.

CREATE TABLE academic_years (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT      NOT NULL,
    year        INT         NOT NULL,
    is_current  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT fk_academic_years_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT uk_academic_years_school_year UNIQUE (school_id, year)
);

CREATE INDEX idx_academic_years_school_id ON academic_years(school_id);
CREATE INDEX idx_academic_years_current ON academic_years(school_id) WHERE is_current = TRUE;