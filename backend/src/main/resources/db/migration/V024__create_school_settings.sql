-- V024__create_school_settings.sql
-- Creates the school_settings table. Per-school configuration (1:1 with schools).

CREATE TABLE school_settings (
    id                 BIGSERIAL PRIMARY KEY,
    school_id          BIGINT    NOT NULL,
    grading_scale      JSONB     NOT NULL,
    report_header      TEXT,
    report_footer      TEXT,
    current_term_id    BIGINT,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP,

    CONSTRAINT fk_school_settings_school_id       FOREIGN KEY (school_id)       REFERENCES schools(id),
    CONSTRAINT fk_school_settings_current_term_id FOREIGN KEY (current_term_id) REFERENCES terms(id),
    CONSTRAINT uk_school_settings_school UNIQUE (school_id)
);