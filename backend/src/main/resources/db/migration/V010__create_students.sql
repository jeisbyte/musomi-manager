-- V010__create_students.sql
-- Creates the students table. Student records.
-- Soft-deletable via is_active. Students are never hard-deleted.

CREATE TABLE students (
    id                  BIGSERIAL PRIMARY KEY,
    school_id           BIGINT       NOT NULL,
    user_id             BIGINT,
    admission_number    VARCHAR(50)  NOT NULL,
    full_name           VARCHAR(200) NOT NULL,
    gender              VARCHAR(10),
    date_of_birth       DATE,
    photo_url           VARCHAR(500),
    current_class_id    BIGINT,
    current_stream_id   BIGINT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP,

    CONSTRAINT fk_students_school_id       FOREIGN KEY (school_id)         REFERENCES schools(id),
    CONSTRAINT fk_students_user_id         FOREIGN KEY (user_id)           REFERENCES users(id),
    CONSTRAINT fk_students_class_id        FOREIGN KEY (current_class_id)  REFERENCES classes(id),
    CONSTRAINT fk_students_stream_id       FOREIGN KEY (current_stream_id) REFERENCES streams(id),
    CONSTRAINT uk_students_school_admission UNIQUE (school_id, admission_number),
    CONSTRAINT ck_students_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL),
    CONSTRAINT ck_students_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'TRANSFERRED', 'GRADUATED', 'DROPPED_OUT', 'SUSPENDED'))
);

CREATE INDEX idx_students_school_id ON students(school_id);
CREATE INDEX idx_students_admission_number ON students(admission_number);
CREATE INDEX idx_students_class_id ON students(current_class_id);
CREATE INDEX idx_students_stream_id ON students(current_stream_id);
CREATE INDEX idx_students_status ON students(status);
CREATE INDEX idx_students_full_name ON students(full_name);