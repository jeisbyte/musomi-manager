-- V022__create_generated_reports.sql
-- Creates the generated_reports table. Stored PDF metadata.

CREATE TABLE generated_reports (
    id               BIGSERIAL PRIMARY KEY,
    school_id        BIGINT       NOT NULL,
    student_id       BIGINT       NOT NULL,
    term_id          BIGINT       NOT NULL,
    file_path        VARCHAR(500) NOT NULL,
    file_size_bytes  BIGINT,
    generated_by     BIGINT       NOT NULL,
    generated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_generated_reports_school_id    FOREIGN KEY (school_id)    REFERENCES schools(id),
    CONSTRAINT fk_generated_reports_student_id   FOREIGN KEY (student_id)   REFERENCES students(id),
    CONSTRAINT fk_generated_reports_term_id      FOREIGN KEY (term_id)      REFERENCES terms(id),
    CONSTRAINT fk_generated_reports_generated_by FOREIGN KEY (generated_by) REFERENCES users(id)
);

CREATE INDEX idx_generated_reports_school_id ON generated_reports(school_id);
CREATE INDEX idx_generated_reports_student_id ON generated_reports(student_id);
CREATE INDEX idx_generated_reports_term_id ON generated_reports(term_id);
CREATE INDEX idx_generated_reports_student_term ON generated_reports(student_id, term_id);