-- V019__create_score_history.sql
-- Creates the score_history table. Audit trail of every score change.

CREATE TABLE score_history (
    id            BIGSERIAL PRIMARY KEY,
    school_id     BIGINT       NOT NULL,
    score_id      BIGINT       NOT NULL,
    old_score     DECIMAL(6,2),
    new_score     DECIMAL(6,2),
    old_feedback  VARCHAR(200),
    new_feedback  VARCHAR(200),
    changed_by    BIGINT       NOT NULL,
    changed_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    reason        VARCHAR(200),

    CONSTRAINT fk_score_history_school_id  FOREIGN KEY (school_id)  REFERENCES schools(id),
    CONSTRAINT fk_score_history_score_id   FOREIGN KEY (score_id)   REFERENCES scores(id),
    CONSTRAINT fk_score_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_score_history_school_id ON score_history(school_id);
CREATE INDEX idx_score_history_score_id ON score_history(score_id);
CREATE INDEX idx_score_history_changed_at ON score_history(changed_at);