-- V009__create_login_history.sql
-- Creates the login_history table. Every login attempt (success and failure).

CREATE TABLE login_history (
    id                  BIGSERIAL PRIMARY KEY,
    school_id           BIGINT       NOT NULL,
    user_id             BIGINT,
    username_attempted  VARCHAR(100) NOT NULL,
    success             BOOLEAN      NOT NULL,
    ip_address          VARCHAR(50),
    user_agent          VARCHAR(500),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_login_history_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_login_history_user_id   FOREIGN KEY (user_id)   REFERENCES users(id)
);

CREATE INDEX idx_login_history_school_id ON login_history(school_id);
CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_created_at ON login_history(created_at);
CREATE INDEX idx_login_history_username ON login_history(username_attempted);