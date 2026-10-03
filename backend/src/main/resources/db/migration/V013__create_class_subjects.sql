-- V013__create_class_subjects.sql
-- Creates the class_subjects table. Which subjects are offered at which class levels.
-- Many-to-many between class_levels and subjects.

CREATE TABLE class_subjects (
    id              BIGSERIAL PRIMARY KEY,
    school_id       BIGINT    NOT NULL,
    class_level_id  BIGINT    NOT NULL,
    subject_id      BIGINT    NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_class_subjects_school_id      FOREIGN KEY (school_id)      REFERENCES schools(id),
    CONSTRAINT fk_class_subjects_class_level_id FOREIGN KEY (class_level_id) REFERENCES class_levels(id),
    CONSTRAINT fk_class_subjects_subject_id     FOREIGN KEY (subject_id)     REFERENCES subjects(id),
    CONSTRAINT uk_class_subjects UNIQUE (class_level_id, subject_id)
);

CREATE INDEX idx_class_subjects_school_id ON class_subjects(school_id);
CREATE INDEX idx_class_subjects_class_level_id ON class_subjects(class_level_id);
CREATE INDEX idx_class_subjects_subject_id ON class_subjects(subject_id);