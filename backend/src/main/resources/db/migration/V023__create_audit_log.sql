-- V023__create_audit_log.sql
-- Creates the audit_log table. Every state-changing action.

CREATE TABLE audit_log (
    id           BIGSERIAL PRIMARY KEY,
    school_id    BIGINT      NOT NULL,
    user_id      BIGINT,
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    BIGINT,
    details      JSONB,
    ip_address   VARCHAR(50),
    user_agent   VARCHAR(500),
    created_at   TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_log_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_audit_log_user_id   FOREIGN KEY (user_id)   REFERENCES users(id)
);

CREATE INDEX idx_audit_log_school_id ON audit_log(school_id);
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_school_created ON audit_log(school_id, created_at DESC);