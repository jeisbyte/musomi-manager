-- V015__create_teacher_assignments.sql
-- Creates the teacher_assignments table. Who teaches what subject to which class.

CREATE TABLE teacher_assignments (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT    NOT NULL,
    teacher_id        BIGINT    NOT NULL,
    subject_id        BIGINT    NOT NULL,
    class_id          BIGINT    NOT NULL,
    stream_id         BIGINT,
    academic_year_id  BIGINT    NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_teacher_assignments_school_id        FOREIGN KEY (school_id)        REFERENCES schools(id),
    CONSTRAINT fk_teacher_assignments_teacher_id       FOREIGN KEY (teacher_id)       REFERENCES users(id),
    CONSTRAINT fk_teacher_assignments_subject_id       FOREIGN KEY (subject_id)       REFERENCES subjects(id),
    CONSTRAINT fk_teacher_assignments_class_id         FOREIGN KEY (class_id)         REFERENCES classes(id),
    CONSTRAINT fk_teacher_assignments_stream_id        FOREIGN KEY (stream_id)        REFERENCES streams(id),
    CONSTRAINT fk_teacher_assignments_academic_year_id FOREIGN KEY (academic_year_id) REFERENCES academic_years(id)
);

CREATE INDEX idx_teacher_assignments_school_id ON teacher_assignments(school_id);
CREATE INDEX idx_teacher_assignments_teacher_id ON teacher_assignments(teacher_id);
CREATE INDEX idx_teacher_assignments_subject_id ON teacher_assignments(subject_id);
CREATE INDEX idx_teacher_assignments_class_id ON teacher_assignments(class_id);
CREATE INDEX idx_teacher_assignments_year ON teacher_assignments(academic_year_id);