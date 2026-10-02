-- V004__create_terms.sql
-- Creates the terms table. Terms within an academic year.
-- Examples: "Term 1", "Term 2", "Term 3".

CREATE TABLE terms (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT      NOT NULL,
    academic_year_id  BIGINT      NOT NULL,
    name              VARCHAR(50) NOT NULL,
    start_date        DATE        NOT NULL,
    end_date          DATE        NOT NULL,
    is_current        BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,

    CONSTRAINT fk_terms_school_id         FOREIGN KEY (school_id)        REFERENCES schools(id),
    CONSTRAINT fk_terms_academic_year_id  FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT ck_terms_dates             CHECK (end_date > start_date)
);

CREATE INDEX idx_terms_school_id ON terms(school_id);
CREATE INDEX idx_terms_academic_year_id ON terms(academic_year_id);
CREATE INDEX idx_terms_current ON terms(school_id) WHERE is_current = TRUE;