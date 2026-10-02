-- V017__create_assessment_topics.sql
-- Creates the assessment_topics table. Topics covered by an assessment.
-- Many-to-many between assessments and topics.

CREATE TABLE assessment_topics (
    id             BIGSERIAL PRIMARY KEY,
    school_id      BIGINT    NOT NULL,
    assessment_id  BIGINT    NOT NULL,
    topic_id       BIGINT    NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_assessment_topics_school_id     FOREIGN KEY (school_id)     REFERENCES schools(id),
    CONSTRAINT fk_assessment_topics_assessment_id FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
    CONSTRAINT fk_assessment_topics_topic_id      FOREIGN KEY (topic_id)      REFERENCES topics(id),
    CONSTRAINT uk_assessment_topics UNIQUE (assessment_id, topic_id)
);

CREATE INDEX idx_assessment_topics_school_id ON assessment_topics(school_id);
CREATE INDEX idx_assessment_topics_assessment_id ON assessment_topics(assessment_id);
CREATE INDEX idx_assessment_topics_topic_id ON assessment_topics(topic_id);