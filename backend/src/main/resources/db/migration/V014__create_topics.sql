-- V014__create_topics.sql
-- Creates the topics table. Topics within a subject (Algebra, Geometry, etc.).

CREATE TABLE topics (
    id           BIGSERIAL PRIMARY KEY,
    school_id    BIGINT       NOT NULL,
    subject_id   BIGINT       NOT NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    sort_order   INT          NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP,

    CONSTRAINT fk_topics_school_id  FOREIGN KEY (school_id)  REFERENCES schools(id),
    CONSTRAINT fk_topics_subject_id FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT uk_topics_subject_name UNIQUE (subject_id, name)
);

CREATE INDEX idx_topics_school_id ON topics(school_id);
CREATE INDEX idx_topics_subject_id ON topics(subject_id);