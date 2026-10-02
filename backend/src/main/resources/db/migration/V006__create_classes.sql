-- V006__create_classes.sql
-- Creates the classes table. A class for a specific academic year.
-- Example: "S3" in 2026. Soft-deletable via is_active.

CREATE TABLE classes (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT      NOT NULL,
    class_level_id    BIGINT      NOT NULL,
    academic_year_id  BIGINT      NOT NULL,
    name              VARCHAR(50) NOT NULL,
    class_teacher_id  BIGINT,
    is_active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,

    CONSTRAINT fk_classes_school_id        FOREIGN KEY (school_id)        REFERENCES schools(id),
    CONSTRAINT fk_classes_class_level_id   FOREIGN KEY (class_level_id)   REFERENCES class_levels(id),
    CONSTRAINT fk_classes_academic_year_id FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_classes_class_teacher_id FOREIGN KEY (class_teacher_id) REFERENCES users(id),
    CONSTRAINT uk_classes_school_year_level UNIQUE (school_id, academic_year_id, class_level_id)
);

CREATE INDEX idx_classes_school_id ON classes(school_id);
CREATE INDEX idx_classes_academic_year_id ON classes(academic_year_id);
CREATE INDEX idx_classes_class_level_id ON classes(class_level_id);
