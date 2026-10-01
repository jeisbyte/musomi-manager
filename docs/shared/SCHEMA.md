# SCHEMA.md — Shared Document

**Every table, every column, every relationship. The source of truth for the database.**

---

## 1. How to Use This Document

- **Database engineer:** writes Flyway migrations from this
- **Backend lead:** writes JPA entities matching this
- **Frontend devs:** understand what fields exist
- **QA:** verifies data integrity
- **Never change the schema without updating this file**
- **Never change this file without team agreement**

If the code and this document disagree, this document wins — until someone updates both.

---

## 2. Conventions

Every table has:

```sql
id BIGSERIAL PRIMARY KEY,
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP
```

Every **school-scoped** table also has:

```sql
school_id BIGINT NOT NULL REFERENCES schools(id)
```

Every **soft-deletable** table also has:

```sql
is_active BOOLEAN NOT NULL DEFAULT TRUE
```

### Naming

| Thing | Convention | Example |
|---|---|---|
| Table | snake_case, plural | `students` |
| Column | snake_case | `admission_number` |
| Foreign key | `<table_singular>_id` | `student_id` |
| Index | `idx_<table>_<column(s)>` | `idx_students_admission_number` |
| Unique | `uk_<table>_<column(s)>` | `uk_students_school_admission` |
| Foreign key constraint | `fk_<table>_<column>` | `fk_students_school_id` |
| Check constraint | `ck_<table>_<rule>` | `ck_scores_score_range` |

### Data types

| Purpose | Type |
|---|---|
| ID | `BIGSERIAL` |
| Foreign key | `BIGINT` |
| Short text | `VARCHAR(n)` |
| Long text | `TEXT` |
| Decimal (marks) | `DECIMAL(6,2)` |
| Boolean | `BOOLEAN` |
| Date only | `DATE` |
| Timestamp | `TIMESTAMP` |
| Enum-like | `VARCHAR(20)` with CHECK |
| JSON | `JSONB` |

---

## 3. Table Overview

| # | Table | Purpose | School-scoped | Soft delete |
|---|---|---|---|---|
| 1 | `schools` | The school itself | — | No |
| 2 | `academic_years` | Years like 2024, 2025, 2026 | Yes | No |
| 3 | `terms` | Terms within a year | Yes* | No |
| 4 | `class_levels` | S1, S2, P1, etc. | Yes | Yes |
| 5 | `classes` | S3, S4 (per year) | Yes | Yes |
| 6 | `streams` | Blue, Red, Green | Yes* | Yes |
| 7 | `users` | All system users | Yes | Yes |
| 8 | `user_sessions` | Active sessions | Yes* | No |
| 9 | `login_history` | Every login attempt | Yes* | No |
| 10 | `students` | Student records | Yes | Yes |
| 11 | `guardians` | Parents/guardians | Yes* | No |
| 12 | `subjects` | Math, English, etc. | Yes | Yes |
| 13 | `class_subjects` | Which subjects per class level | Yes* | No |
| 14 | `topics` | Algebra, Geometry, etc. | Yes* | No |
| 15 | `teacher_assignments` | Who teaches what | Yes* | No |
| 16 | `assessments` | Tests, exams, etc. | Yes | No |
| 17 | `assessment_topics` | Topics covered by assessment | Yes* | No |
| 18 | `scores` | Student marks | Yes* | No |
| 19 | `score_history` | Every score change | Yes* | No |
| 20 | `comments` | Teacher comments | Yes | No |
| 21 | `report_requests` | Report generation requests | Yes | No |
| 22 | `generated_reports` | Stored PDF metadata | Yes | No |
| 23 | `audit_log` | Every action | Yes | No |
| 24 | `school_settings` | Per-school config | Yes | No |
| 25 | `import_jobs` | Import history | Yes | No |

*Tables marked `Yes*` have `school_id` inherited via foreign key, not stored directly.

**Actually — for simplicity in v1, every school-scoped table stores `school_id` directly.** This makes queries simpler and multi-tenancy bulletproof. So all tables except `schools` have `school_id`.

Let me correct that:

| # | Table | School-scoped |
|---|---|---|
| 1 | `schools` | No |
| 2–25 | All others | Yes |

---

## 4. Full Table Definitions

### 1. schools

The school itself. One row per school. In v1, only one row.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| name | VARCHAR(200) | No | — | School name |
| logo_url | VARCHAR(500) | Yes | NULL | Path to uploaded logo |
| address | TEXT | Yes | NULL | |
| phone | VARCHAR(50) | Yes | NULL | |
| email | VARCHAR(100) | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`

**Indexes:**
- None needed (tiny table)

---

### 2. academic_years

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| year | INT | No | — | e.g. 2026 |
| is_current | BOOLEAN | No | FALSE | Only one per school |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_users_school` → schools(id)
- Unique: `uq_users_school_username` (school_id, username)
- Check: `chk_users_role` (role IN ('ADMIN', 'TEACHER', 'STUDENT'))

**Indexes:**
- `idx_users_school_id`
- `idx_users_username`
- `idx_users_role`
- `idx_users_school_active` on (school_id) WHERE is_active = TRUE
---
> **Note:** V002 predates the naming standard in `DATABASE.md` §4. V003+ will follow the standard.

### 3. terms

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| academic_year_id | BIGINT | No | — | FK → academic_years |
| name | VARCHAR(50) | No | — | "Term 1" |
| start_date | DATE | No | — | |
| end_date | DATE | No | — | |
| is_current | BOOLEAN | No | FALSE | Only one per school |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_terms_school_id` → schools(id)
- FK: `fk_terms_academic_year_id` → academic_years(id)
- Check: `ck_terms_dates` (end_date > start_date)

**Indexes:**
- `idx_terms_school_id`
- `idx_terms_academic_year_id`
- `idx_terms_current` on (school_id, is_current) WHERE is_current = TRUE

---

### 4. class_levels

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| name | VARCHAR(20) | No | — | "S1", "S2", "P1" |
| sort_order | INT | No | — | For ordering |
| is_active | BOOLEAN | No | TRUE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_class_levels_school_id` → schools(id)
- Unique: `uk_class_levels_school_name` (school_id, name)

**Indexes:**
- `idx_class_levels_school_id`

---

### 5. classes

A class for a specific academic year (S3 in 2026).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| class_level_id | BIGINT | No | — | FK → class_levels |
| academic_year_id | BIGINT | No | — | FK → academic_years |
| name | VARCHAR(50) | No | — | "S3" |
| class_teacher_id | BIGINT | Yes | NULL | FK → users |
| is_active | BOOLEAN | No | TRUE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_classes_school_id` → schools(id)
- FK: `fk_classes_class_level_id` → class_levels(id)
- FK: `fk_classes_academic_year_id` → academic_years(id)
- FK: `fk_classes_class_teacher_id` → users(id)
- Unique: `uk_classes_school_year_level` (school_id, academic_year_id, class_level_id)

**Indexes:**
- `idx_classes_school_id`
- `idx_classes_academic_year_id`
- `idx_classes_class_level_id`

---

### 6. streams

Streams within a class (S3 Blue, S3 Red).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| class_id | BIGINT | No | — | FK → classes |
| name | VARCHAR(50) | No | — | "Blue" |
| capacity | INT | Yes | NULL | |
| is_active | BOOLEAN | No | TRUE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_streams_school_id` → schools(id)
- FK: `fk_streams_class_id` → classes(id)
- Unique: `uk_streams_class_name` (class_id, name)

**Indexes:**
- `idx_streams_school_id`
- `idx_streams_class_id`

---

### 7. users

All system users (admins, teachers, students).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| username | VARCHAR(100) | No | — | Unique per school |
| password_hash | VARCHAR(255) | No | — | BCrypt |
| full_name | VARCHAR(200) | No | — | |
| email | VARCHAR(100) | Yes | NULL | |
| phone | VARCHAR(50) | Yes | NULL | |
| role | VARCHAR(20) | No | — | ADMIN, TEACHER, STUDENT |
| is_active | BOOLEAN | No | TRUE | |
| failed_login_attempts | INT | No | 0 | |
| locked_until | TIMESTAMP | Yes | NULL | |
| last_login_at | TIMESTAMP | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_users_school_id` → schools(id)
- Unique: `uk_users_school_username` (school_id, username)
- Check: `ck_users_role` (role IN ('ADMIN', 'TEACHER', 'STUDENT'))

**Indexes:**
- `idx_users_school_id`
- `idx_users_username`
- `idx_users_role`
- `idx_users_active` on (school_id, is_active) WHERE is_active = TRUE

---

### 8. user_sessions

Active sessions (for JWT tracking and revocation).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| user_id | BIGINT | No | — | FK → users |
| token_hash | VARCHAR(255) | No | — | SHA-256 of token |
| ip_address | VARCHAR(50) | Yes | NULL | |
| user_agent | VARCHAR(500) | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |
| expires_at | TIMESTAMP | No | — | |
| revoked_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_user_sessions_school_id` → schools(id)
- FK: `fk_user_sessions_user_id` → users(id)

**Indexes:**
- `idx_user_sessions_school_id`
- `idx_user_sessions_user_id`
- `idx_user_sessions_token_hash`
- `idx_user_sessions_expires_at`

---

### 9. login_history

Every login attempt (success and failure).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| user_id | BIGINT | Yes | NULL | FK → users (null if user not found) |
| username_attempted | VARCHAR(100) | No | — | |
| success | BOOLEAN | No | — | |
| ip_address | VARCHAR(50) | Yes | NULL | |
| user_agent | VARCHAR(500) | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_login_history_school_id` → schools(id)
- FK: `fk_login_history_user_id` → users(id)

**Indexes:**
- `idx_login_history_school_id`
- `idx_login_history_user_id`
- `idx_login_history_created_at`
- `idx_login_history_username`

---

### 10. students

Student records.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| user_id | BIGINT | Yes | NULL | FK → users (login account) |
| admission_number | VARCHAR(50) | No | — | |
| full_name | VARCHAR(200) | No | — | |
| gender | VARCHAR(10) | Yes | NULL | MALE, FEMALE, OTHER |
| date_of_birth | DATE | Yes | NULL | |
| photo_url | VARCHAR(500) | Yes | NULL | |
| current_class_id | BIGINT | Yes | NULL | FK → classes |
| current_stream_id | BIGINT | Yes | NULL | FK → streams |
| status | VARCHAR(20) | No | 'ACTIVE' | |
| is_active | BOOLEAN | No | TRUE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_students_school_id` → schools(id)
- FK: `fk_students_user_id` → users(id)
- FK: `fk_students_class_id` → classes(id)
- FK: `fk_students_stream_id` → streams(id)
- Unique: `uk_students_school_admission` (school_id, admission_number)
- Check: `ck_students_gender` (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL)
- Check: `ck_students_status` (status IN ('ACTIVE', 'INACTIVE', 'TRANSFERRED', 'GRADUATED', 'DROPPED_OUT', 'SUSPENDED'))

**Indexes:**
- `idx_students_school_id`
- `idx_students_admission_number`
- `idx_students_class_id`
- `idx_students_stream_id`
- `idx_students_status`
- `idx_students_full_name` (for search)

---

### 11. guardians

Guardians of students.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| student_id | BIGINT | No | — | FK → students |
| name | VARCHAR(200) | No | — | |
| relationship | VARCHAR(50) | No | — | Mother, Father, Guardian |
| phone | VARCHAR(50) | No | — | |
| email | VARCHAR(100) | Yes | NULL | |
| is_primary | BOOLEAN | No | FALSE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_guardians_school_id` → schools(id)
- FK: `fk_guardians_student_id` → students(id) ON DELETE CASCADE

**Indexes:**
- `idx_guardians_school_id`
- `idx_guardians_student_id`

---

### 12. subjects

Subjects per school.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| code | VARCHAR(20) | No | — | MATH, ENG, BIO |
| name | VARCHAR(100) | No | — | "Mathematics" |
| description | TEXT | Yes | NULL | |
| is_core | BOOLEAN | No | FALSE | |
| is_active | BOOLEAN | No | TRUE | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_subjects_school_id` → schools(id)
- Unique: `uk_subjects_school_code` (school_id, code)

**Indexes:**
- `idx_subjects_school_id`
- `idx_subjects_code`

---

### 13. class_subjects

Which subjects are offered at which class levels.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| class_level_id | BIGINT | No | — | FK → class_levels |
| subject_id | BIGINT | No | — | FK → subjects |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_class_subjects_school_id` → schools(id)
- FK: `fk_class_subjects_class_level_id` → class_levels(id)
- FK: `fk_class_subjects_subject_id` → subjects(id)
- Unique: `uk_class_subjects` (class_level_id, subject_id)

**Indexes:**
- `idx_class_subjects_school_id`
- `idx_class_subjects_class_level_id`
- `idx_class_subjects_subject_id`

---

### 14. topics

Topics within a subject.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| subject_id | BIGINT | No | — | FK → subjects |
| name | VARCHAR(150) | No | — | "Algebra" |
| description | TEXT | Yes | NULL | |
| sort_order | INT | No | — | |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_topics_school_id` → schools(id)
- FK: `fk_topics_subject_id` → subjects(id)
- Unique: `uk_topics_subject_name` (subject_id, name)

**Indexes:**
- `idx_topics_school_id`
- `idx_topics_subject_id`

---

### 15. teacher_assignments

Who teaches what subject to which class.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| teacher_id | BIGINT | No | — | FK → users |
| subject_id | BIGINT | No | — | FK → subjects |
| class_id | BIGINT | No | — | FK → classes |
| stream_id | BIGINT | Yes | NULL | FK → streams (null = whole class) |
| academic_year_id | BIGINT | No | — | FK → academic_years |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_teacher_assignments_school_id` → schools(id)
- FK: `fk_teacher_assignments_teacher_id` → users(id)
- FK: `fk_teacher_assignments_subject_id` → subjects(id)
- FK: `fk_teacher_assignments_class_id` → classes(id)
- FK: `fk_teacher_assignments_stream_id` → streams(id)
- FK: `fk_teacher_assignments_academic_year_id` → academic_years(id)

**Indexes:**
- `idx_teacher_assignments_school_id`
- `idx_teacher_assignments_teacher_id`
- `idx_teacher_assignments_subject_id`
- `idx_teacher_assignments_class_id`
- `idx_teacher_assignments_year`

---

### 16. assessments

Assessments created by teachers.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| teacher_id | BIGINT | No | — | FK → users |
| class_id | BIGINT | No | — | FK → classes |
| stream_id | BIGINT | Yes | NULL | FK → streams |
| subject_id | BIGINT | No | — | FK → subjects |
| term_id | BIGINT | No | — | FK → terms |
| title | VARCHAR(200) | No | — | |
| type | VARCHAR(30) | No | — | TEST, EXAM, QUIZ, etc. |
| assessment_date | DATE | No | — | |
| max_score | DECIMAL(6,2) | No | — | |
| status | VARCHAR(20) | No | 'DRAFT' | DRAFT, PUBLISHED |
| published_at | TIMESTAMP | Yes | NULL | |
| published_by | BIGINT | Yes | NULL | FK → users |
| created_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_assessments_school_id` → schools(id)
- FK: `fk_assessments_teacher_id` → users(id)
- FK: `fk_assessments_class_id` → classes(id)
- FK: `fk_assessments_stream_id` → streams(id)
- FK: `fk_assessments_subject_id` → subjects(id)
- FK: `fk_assessments_term_id` → terms(id)
- FK: `fk_assessments_published_by` → users(id)
- Check: `ck_assessments_type` (type IN ('TEST', 'EXAM', 'QUIZ', 'HOMEWORK', 'PROJECT', 'PRACTICAL', 'ORAL'))
- Check: `ck_assessments_status` (status IN ('DRAFT', 'PUBLISHED'))
- Check: `ck_assessments_max_score` (max_score > 0)

**Indexes:**
- `idx_assessments_school_id`
- `idx_assessments_teacher_id`
- `idx_assessments_class_id`
- `idx_assessments_subject_id`
- `idx_assessments_term_id`
- `idx_assessments_status`
- `idx_assessments_class_subject_term` (class_id, subject_id, term_id)

---

### 17. assessment_topics

Topics covered by an assessment (many-to-many).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| assessment_id | BIGINT | No | — | FK → assessments |
| topic_id | BIGINT | No | — | FK → topics |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_assessment_topics_school_id` → schools(id)
- FK: `fk_assessment_topics_assessment_id` → assessments(id) ON DELETE CASCADE
- FK: `fk_assessment_topics_topic_id` → topics(id)
- Unique: `uk_assessment_topics` (assessment_id, topic_id)

**Indexes:**
- `idx_assessment_topics_school_id`
- `idx_assessment_topics_assessment_id`
- `idx_assessment_topics_topic_id`

---

### 18. scores

Student marks for each assessment.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| assessment_id | BIGINT | No | — | FK → assessments |
| student_id | BIGINT | No | — | FK → students |
| score | DECIMAL(6,2) | Yes | NULL | Null = not entered |
| feedback | VARCHAR(200) | Yes | NULL | |
| entered_by | BIGINT | Yes | NULL | FK → users |
| entered_at | TIMESTAMP | Yes | NULL | |
| updated_at | TIMESTAMP | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_scores_school_id` → schools(id)
- FK: `fk_scores_assessment_id` → assessments(id)
- FK: `fk_scores_student_id` → students(id)
- FK: `fk_scores_entered_by` → users(id)
- Unique: `uk_scores_assessment_student` (assessment_id, student_id)
- Check: `ck_scores_score_range` (score IS NULL OR score >= 0)

**Indexes:**
- `idx_scores_school_id`
- `idx_scores_assessment_id`
- `idx_scores_student_id`
- `idx_scores_student_assessment` (student_id, assessment_id)

**Note:** The upper bound (score <= max_score) is validated in the app because `max_score` varies per assessment.

---

### 19. score_history

Every change to a score (audit trail).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| score_id | BIGINT | No | — | FK → scores |
| old_score | DECIMAL(6,2) | Yes | NULL | |
| new_score | DECIMAL(6,2) | Yes | NULL | |
| old_feedback | VARCHAR(200) | Yes | NULL | |
| new_feedback | VARCHAR(200) | Yes | NULL | |
| changed_by | BIGINT | No | — | FK → users |
| changed_at | TIMESTAMP | No | NOW() | |
| reason | VARCHAR(200) | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_score_history_school_id` → schools(id)
- FK: `fk_score_history_score_id` → scores(id)
- FK: `fk_score_history_changed_by` → users(id)

**Indexes:**
- `idx_score_history_school_id`
- `idx_score_history_score_id`
- `idx_score_history_changed_at`

---

### 20. comments

Teacher comments per student per term.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| student_id | BIGINT | No | — | FK → students |
| term_id | BIGINT | No | — | FK → terms |
| subject_id | BIGINT | Yes | NULL | FK → subjects (null = class teacher comment) |
| comment_type | VARCHAR(30) | No | — | SUBJECT, CLASS_TEACHER, HEAD_TEACHER |
| comment_text | TEXT | No | — | |
| written_by | BIGINT | No | — | FK → users |
| written_at | TIMESTAMP | No | NOW() | |
| updated_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_comments_school_id` → schools(id)
- FK: `fk_comments_student_id` → students(id)
- FK: `fk_comments_term_id` → terms(id)
- FK: `fk_comments_subject_id` → subjects(id)
- FK: `fk_comments_written_by` → users(id)
- Check: `ck_comments_type` (comment_type IN ('SUBJECT', 'CLASS_TEACHER', 'HEAD_TEACHER'))

**Indexes:**
- `idx_comments_school_id`
- `idx_comments_student_id`
- `idx_comments_term_id`
- `idx_comments_student_term` (student_id, term_id)

---

### 21. report_requests

Requests to generate report cards (workflow tracking).

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| student_id | BIGINT | No | — | FK → students |
| term_id | BIGINT | No | — | FK → terms |
| status | VARCHAR(20) | No | 'PENDING' | PENDING, GENERATED, FAILED |
| requested_by | BIGINT | No | — | FK → users |
| requested_at | TIMESTAMP | No | NOW() | |
| processed_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_report_requests_school_id` → schools(id)
- FK: `fk_report_requests_student_id` → students(id)
- FK: `fk_report_requests_term_id` → terms(id)
- FK: `fk_report_requests_requested_by` → users(id)
- Check: `ck_report_requests_status` (status IN ('PENDING', 'GENERATED', 'FAILED'))

**Indexes:**
- `idx_report_requests_school_id`
- `idx_report_requests_student_id`
- `idx_report_requests_status`

---

### 22. generated_reports

Stored PDFs.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| student_id | BIGINT | No | — | FK → students |
| term_id | BIGINT | No | — | FK → terms |
| file_path | VARCHAR(500) | No | — | Relative path on disk |
| file_size_bytes | BIGINT | Yes | NULL | |
| generated_by | BIGINT | No | — | FK → users |
| generated_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_generated_reports_school_id` → schools(id)
- FK: `fk_generated_reports_student_id` → students(id)
- FK: `fk_generated_reports_term_id` → terms(id)
- FK: `fk_generated_reports_generated_by` → users(id)

**Indexes:**
- `idx_generated_reports_school_id`
- `idx_generated_reports_student_id`
- `idx_generated_reports_term_id`
- `idx_generated_reports_student_term` (student_id, term_id)

---

### 23. audit_log

Every state-changing action.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| user_id | BIGINT | Yes | NULL | FK → users |
| action | VARCHAR(50) | No | — | SCORE_UPDATED, etc. |
| entity_type | VARCHAR(50) | No | — | Score, Student, etc. |
| entity_id | BIGINT | Yes | NULL | |
| details | JSONB | Yes | NULL | |
| ip_address | VARCHAR(50) | Yes | NULL | |
| user_agent | VARCHAR(500) | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_audit_log_school_id` → schools(id)
- FK: `fk_audit_log_user_id` → users(id)

**Indexes:**
- `idx_audit_log_school_id`
- `idx_audit_log_user_id`
- `idx_audit_log_action`
- `idx_audit_log_entity`
- `idx_audit_log_created_at`
- `idx_audit_log_school_created` (school_id, created_at DESC)

---

### 24. school_settings

Per-school configuration.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools, UNIQUE |
| grading_scale | JSONB | No | — | Array of {grade, min, max} |
| report_header | TEXT | Yes | NULL | |
| report_footer | TEXT | Yes | NULL | |
| current_term_id | BIGINT | Yes | NULL | FK → terms |
| updated_at | TIMESTAMP | Yes | NULL | |
| created_at | TIMESTAMP | No | NOW() | |

**Constraints:**
- PK: `id`
- FK: `fk_school_settings_school_id` → schools(id)
- FK: `fk_school_settings_current_term_id` → terms(id)
- Unique: `uk_school_settings_school` (school_id)

**Indexes:**
- `idx_school_settings_school_id`

---

### 25. import_jobs

History of Excel imports.

| Column | Type | Null | Default | Notes |
|---|---|---|---|---|
| id | BIGSERIAL | No | — | PK |
| school_id | BIGINT | No | — | FK → schools |
| job_reference | VARCHAR(50) | No | — | e.g. imp_20260924_001 |
| import_type | VARCHAR(20) | No | — | STUDENTS |
| status | VARCHAR(20) | No | — | PENDING, CONFIRMED, CANCELLED, FAILED |
| total_rows | INT | No | 0 | |
| valid_rows | INT | No | 0 | |
| error_rows | INT | No | 0 | |
| errors_json | JSONB | Yes | NULL | |
| original_filename | VARCHAR(255) | Yes | NULL | |
| created_by | BIGINT | No | — | FK → users |
| created_at | TIMESTAMP | No | NOW() | |
| confirmed_at | TIMESTAMP | Yes | NULL | |

**Constraints:**
- PK: `id`
- FK: `fk_import_jobs_school_id` → schools(id)
- FK: `fk_import_jobs_created_by` → users(id)
- Unique: `uk_import_jobs_reference` (job_reference)
- Check: `ck_import_jobs_type` (import_type IN ('STUDENTS'))
- Check: `ck_import_jobs_status` (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'FAILED'))

**Indexes:**
- `idx_import_jobs_school_id`
- `idx_import_jobs_job_reference`

---

## 5. Relationships Summary

```
schools
  ├── academic_years
  │     └── terms
  │           ├── assessments
  │           ├── comments
  │           ├── report_requests
  │           └── generated_reports
  │
  ├── class_levels
  │     └── classes
  │           ├── streams
  │           │     └── students
  │           └── assessments
  │
  ├── users
  │     ├── students (1:1 via user_id)
  │     ├── teacher_assignments
  │     ├── assessments
  │     ├── scores (entered_by)
  │     ├── comments
  │     ├── audit_log
  │     └── login_history
  │
  ├── students
  │     ├── guardians
  │     ├── scores
  │     ├── comments
  │     └── generated_reports
  │
  ├── subjects
  │     ├── class_subjects
  │     ├── topics
  │     │     └── assessment_topics
  │     ├── assessments
  │     └── comments
  │
  ├── assessments
  │     ├── assessment_topics
  │     └── scores
  │           └── score_history
  │
  └── school_settings (1:1)
```

---

## 6. Common Queries

### Find a student's marks for a term

```sql
SELECT sc.score, sc.feedback, a.title, a.max_score, sub.name AS subject
FROM scores sc
JOIN assessments a ON a.id = sc.assessment_id
JOIN subjects sub ON sub.id = a.subject_id
WHERE sc.student_id = ?
  AND a.term_id = ?
  AND a.status = 'PUBLISHED'
ORDER BY a.assessment_date DESC;
```

### Student average per subject for a term

```sql
SELECT sub.name,
       AVG(sc.score / a.max_score * 100) AS avg_pct,
       COUNT(*) AS count
FROM scores sc
JOIN assessments a ON a.id = sc.assessment_id
JOIN subjects sub ON sub.id = a.subject_id
WHERE sc.student_id = ?
  AND a.term_id = ?
  AND a.status = 'PUBLISHED'
GROUP BY sub.id, sub.name;
```

### Class ranking

```sql
WITH avg_per_student AS (
    SELECT sc.student_id, AVG(sc.score / a.max_score * 100) AS avg_pct
    FROM scores sc
    JOIN assessments a ON a.id = sc.assessment_id
    WHERE a.class_id = ? AND a.stream_id = ? AND a.term_id = ?
      AND a.status = 'PUBLISHED'
    GROUP BY sc.student_id
)
SELECT student_id,
       RANK() OVER (ORDER BY avg_pct DESC) AS position
FROM avg_per_student;
```

---

## 7. Seed Data (v1)

For development and testing, the `V099__test_data.sql` migration seeds:

- 1 school
- 3 academic years
- 9 terms (3 per year)
- 6 class levels
- 2 classes (S3, S4) in 2026
- 3 streams (S3 Blue, S3 Red, S4 Blue)
- 3 users (1 admin, 2 teachers)
- 50 students in S3 Blue
- 5 subjects
- 4 topics per subject
- Teacher assignments
- 1 published assessment with scores

Full seed file lives in `V099__test_data.sql`.

---

## 8. Table Count

**25 tables in v1.**

Why not 45 like the full version?

- No AI tables (v4)
- No analytics tables (v3)
- No notification tables (v2a)
- No sharing tables (v2b)
- No offline tables (v2a)
- No equity tables (v5)
- No multi-school management tables (v5)

**v1 is a subset. That's the point.**

---

## 9. Reference Documents

- `MASTER.md` — project context
- `docs/shared/API_CONTRACT.md` — endpoints that use these tables
- `docs/specialists/BACKEND.md` — entity conventions
- `docs/specialists/DATABASE.md` — migration conventions

---

## The One-Sentence Summary

**SCHEMA.md defines all 25 tables for v1 — every column, every constraint, every index, every relationship. Every table has `school_id` (multi-tenancy from day one). Students and scores are never hard-deleted. The database engineer writes Flyway migrations from this. The backend lead writes JPA entities matching this.**

---

