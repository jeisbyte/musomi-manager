-- V016__create_assessments.sql
-- Creates the assessments table. Tests, exams, quizzes created by teachers.

CREATE TABLE assessments (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT       NOT NULL,
    teacher_id        BIGINT       NOT NULL,
    class_id          BIGINT       NOT NULL,
    stream_id         BIGINT,
    subject_id        BIGINT       NOT NULL,
    term_id           BIGINT       NOT NULL,
    title             VARCHAR(200) NOT NULL,
    type              VARCHAR(30)  NOT NULL,
    assessment_date   DATE         NOT NULL,
    max_score         DECIMAL(6,2) NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    published_at      TIMESTAMP,
    published_by      BIGINT,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,

    CONSTRAINT fk_assessments_school_id   FOREIGN KEY (school_id)    REFERENCES schools(id),
    CONSTRAINT fk_assessments_teacher_id  FOREIGN KEY (teacher_id)   REFERENCES users(id),
    CONSTRAINT fk_assessments_class_id    FOREIGN KEY (class_id)     REFERENCES classes(id),
    CONSTRAINT fk_assessments_stream_id   FOREIGN KEY (stream_id)    REFERENCES streams(id),
    CONSTRAINT fk_assessments_subject_id  FOREIGN KEY (subject_id)   REFERENCES subjects(id),
    CONSTRAINT fk_assessments_term_id     FOREIGN KEY (term_id)      REFERENCES terms(id),
    CONSTRAINT fk_assessments_published_by FOREIGN KEY (published_by) REFERENCES users(id),
    CONSTRAINT ck_assessments_type CHECK (type IN ('TEST', 'EXAM', 'QUIZ', 'HOMEWORK', 'PROJECT', 'PRACTICAL', 'ORAL')),
    CONSTRAINT ck_assessments_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT ck_assessments_max_score CHECK (max_score > 0)
);

CREATE INDEX idx_assessments_school_id ON assessments(school_id);
CREATE INDEX idx_assessments_teacher_id ON assessments(teacher_id);
CREATE INDEX idx_assessments_class_id ON assessments(class_id);
CREATE INDEX idx_assessments_subject_id ON assessments(subject_id);
CREATE INDEX idx_assessments_term_id ON assessments(term_id);
CREATE INDEX idx_assessments_status ON assessments(status);
CREATE INDEX idx_assessments_class_subject_term ON assessments(class_id, subject_id, term_id);