-- V007__create_streams.sql
-- Creates the streams table. Streams within a class (S3 Blue, S3 Red).
-- Soft-deletable via is_active.

CREATE TABLE streams (
    id          BIGSERIAL PRIMARY KEY,
    school_id   BIGINT      NOT NULL,
    class_id    BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    capacity    INT,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT fk_streams_school_id FOREIGN KEY (school_id) REFERENCES schools(id),
    CONSTRAINT fk_streams_class_id  FOREIGN KEY (class_id)  REFERENCES classes(id),
    CONSTRAINT uk_streams_class_name UNIQUE (class_id, name)
);

CREATE INDEX idx_streams_school_id ON streams(school_id);
CREATE INDEX idx_streams_class_id ON streams(class_id);