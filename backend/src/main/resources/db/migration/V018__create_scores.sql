-- V018__create_scores.sql
-- Creates the scores table. Student marks for each assessment.
-- Scores are never hard-deleted (see SCHEMA.md design rule #4).

CREATE TABLE scores (
    id             BIGSERIAL PRIMARY KEY,
    school_id      BIGINT       NOT NULL,
    assessment_id  BIGINT       NOT NULL,
    student_id     BIGINT       NOT NULL,
    score          DECIMAL(6,2),
    feedback       VARCHAR(200),
    entered_by     BIGINT,
    entered_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_scores_school_id     FOREIGN KEY (school_id)     REFERENCES schools(id),
    CONSTRAINT fk_scores_assessment_id FOREIGN KEY (assessment_id) REFERENCES assessments(id),
    CONSTRAINT fk_scores_student_id    FOREIGN KEY (student_id)    REFERENCES students(id),
    CONSTRAINT fk_scores_entered_by    FOREIGN KEY (entered_by)    REFERENCES users(id),
    CONSTRAINT uk_scores_assessment_student UNIQUE (assessment_id, student_id),
    CONSTRAINT ck_scores_score_range CHECK (score IS NULL OR score >= 0)
);

CREATE INDEX idx_scores_school_id ON scores(school_id);
CREATE INDEX idx_scores_assessment_id ON scores(assessment_id);
CREATE INDEX idx_scores_student_id ON scores(student_id);
CREATE INDEX idx_scores_student_assessment ON scores(student_id, assessment_id);
