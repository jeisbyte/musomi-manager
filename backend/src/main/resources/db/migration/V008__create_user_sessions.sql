-- V008__create_user_sessions.sql
-- Creates the user_sessions table. Active JWT sessions for tracking/revocation.

CREATE TABLE user_sessions (
    id           BIGSERIAL PRIMARY KEY,
    school_id    BIGINT       NOT NULL,
    user_id      BIGINT       NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    ip_address   VARCHAR(50),
    user_agent   VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at   TIMESTAMP    NOT NULL,
    revoked_at   TIMESTAMP,

    CONSTRAINT fk_user_sessions_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_user_sessions_user_id   FOREIGN KEY (user_id)   REFERENCES users(id)
);

CREATE INDEX idx_user_sessions_school_id ON user_sessions(school_id);
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);