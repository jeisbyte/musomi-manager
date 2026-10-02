-- V020__create_comments.sql
-- Creates the comments table. Teacher comments per student per term.

CREATE TABLE comments (
    id            BIGSERIAL PRIMARY KEY,
    school_id     BIGINT      NOT NULL,
    student_id    BIGINT      NOT NULL,
    term_id       BIGINT      NOT NULL,
    subject_id    BIGINT,
    comment_type  VARCHAR(30) NOT NULL,
    comment_text  TEXT        NOT NULL,
    written_by    BIGINT      NOT NULL,
    written_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP,

    CONSTRAINT fk_comments_school_id  FOREIGN KEY (school_id)  REFERENCES schools(id),
    CONSTRAINT fk_comments_student_id FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_comments_term_id    FOREIGN KEY (term_id)    REFERENCES terms(id),
    CONSTRAINT fk_comments_subject_id FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_comments_written_by FOREIGN KEY (written_by) REFERENCES users(id),
    CONSTRAINT ck_comments_type CHECK (comment_type IN ('SUBJECT', 'CLASS_TEACHER', 'HEAD_TEACHER'))
);

CREATE INDEX idx_comments_school_id ON comments(school_id);
CREATE INDEX idx_comments_student_id ON comments(student_id);
CREATE INDEX idx_comments_term_id ON comments(term_id);
CREATE INDEX idx_comments_student_term ON comments(student_id, term_id);