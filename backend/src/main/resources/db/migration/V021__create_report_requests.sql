-- V021__create_report_requests.sql
-- Creates the report_requests table. Workflow tracking for report generation.

CREATE TABLE report_requests (
    id            BIGSERIAL PRIMARY KEY,
    school_id     BIGINT      NOT NULL,
    student_id    BIGINT      NOT NULL,
    term_id       BIGINT      NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by  BIGINT      NOT NULL,
    requested_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    processed_at  TIMESTAMP,

    CONSTRAINT fk_report_requests_school_id    FOREIGN KEY (school_id)    REFERENCES schools(id),
    CONSTRAINT fk_report_requests_student_id   FOREIGN KEY (student_id)   REFERENCES students(id),
    CONSTRAINT fk_report_requests_term_id      FOREIGN KEY (term_id)      REFERENCES terms(id),
    CONSTRAINT fk_report_requests_requested_by FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT ck_report_requests_status CHECK (status IN ('PENDING', 'GENERATED', 'FAILED'))
);

CREATE INDEX idx_report_requests_school_id ON report_requests(school_id);
CREATE INDEX idx_report_requests_student_id ON report_requests(student_id);
CREATE INDEX idx_report_requests_status ON report_requests(status);