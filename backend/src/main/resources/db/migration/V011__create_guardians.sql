-- V011__create_guardians.sql
-- Creates the guardians table. Parents/guardians of students.

CREATE TABLE guardians (
    id            BIGSERIAL PRIMARY KEY,
    school_id     BIGINT       NOT NULL,
    student_id    BIGINT       NOT NULL,
    name          VARCHAR(200) NOT NULL,
    relationship  VARCHAR(50)  NOT NULL,
    phone         VARCHAR(50)  NOT NULL,
    email         VARCHAR(100),
    is_primary    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,

    CONSTRAINT fk_guardians_school_id  FOREIGN KEY (school_id)  REFERENCES schools(id),
    CONSTRAINT fk_guardians_student_id FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE INDEX idx_guardians_school_id ON guardians(school_id);
CREATE INDEX idx_guardians_student_id ON guardians(student_id);