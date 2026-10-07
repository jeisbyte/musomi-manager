-- V005__create_class_levels.sql
-- Creates the class_levels table. Levels like S1, S2, P1 per school.
-- Soft-deletable via is_active.

CREATE TABLE class_levels (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT      NOT NULL,
    name        VARCHAR(20) NOT NULL,
    sort_order  INT         NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT fk_class_levels_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT uk_class_levels_school_name UNIQUE (school_id, name)
);

CREATE INDEX idx_class_levels_school_id ON class_levels(school_id);