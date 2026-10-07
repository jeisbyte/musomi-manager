-- V099__test_data.sql
-- Seed data for local development and testing.
-- Creates: 1 school, 3 years, 9 terms, 6 class levels.
-- Uses hardcoded IDs. Run AFTER V001-V025.

-- ============================================================
-- School
-- ============================================================
INSERT INTO schools (id, name, address, phone, email)
VALUES (1, 'Musomi Secondary School', 'Kampala, Uganda', '+256700000000', 'info@musomi.test');

-- ============================================================
-- Academic Years (2024, 2025, 2026 — 2026 is current)
-- ============================================================
INSERT INTO academic_years (id, school_id, year, is_current) VALUES
    (1, 1, 2024, FALSE),
    (2, 1, 2025, FALSE),
    (3, 1, 2026, TRUE);

-- ============================================================
-- Terms (3 per year = 9 total)
-- ============================================================
INSERT INTO terms (id, school_id, academic_year_id, name, start_date, end_date, is_current) VALUES
    -- 2024
    (1, 1, 1, 'Term 1', '2024-02-01', '2024-04-30', FALSE),
    (2, 1, 1, 'Term 2', '2024-05-20', '2024-08-20', FALSE),
    (3, 1, 1, 'Term 3', '2024-09-10', '2024-11-30', FALSE),
    -- 2025
    (4, 1, 2, 'Term 1', '2025-02-01', '2025-04-30', FALSE),
    (5, 1, 2, 'Term 2', '2025-05-20', '2025-08-20', FALSE),
    (6, 1, 2, 'Term 3', '2025-09-10', '2025-11-30', FALSE),
    -- 2026 (Term 1 is current)
    (7, 1, 3, 'Term 1', '2026-02-01', '2026-04-30', TRUE),
    (8, 1, 3, 'Term 2', '2026-05-20', '2026-08-20', FALSE),
    (9, 1, 3, 'Term 3', '2026-09-10', '2026-11-30', FALSE);

-- ============================================================
-- Class Levels (S1-S6)
-- ============================================================
INSERT INTO class_levels (id, school_id, name, sort_order, is_active) VALUES
    (1, 1, 'S1', 1, TRUE),
    (2, 1, 'S2', 2, TRUE),
    (3, 1, 'S3', 3, TRUE),
    (4, 1, 'S4', 4, TRUE),
    (5, 1, 'S5', 5, TRUE),
    (6, 1, 'S6', 6, TRUE);




    
-- ============================================================
-- Users (1 admin, 2 teachers)
-- NOTE: password_hash is a BCrypt hash of "password123"
-- ============================================================
INSERT INTO users (id, school_id, username, password_hash, full_name, email, role, is_active) VALUES
    (1, 1, 'admin',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Administrator', 'admin@musomi.test',   'ADMIN',   TRUE),
    (2, 1, 'teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Teacher',         'teacher1@musomi.test', 'TEACHER', TRUE),
    (3, 1, 'teacher2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Teacher',         'teacher2@musomi.test', 'TEACHER', TRUE);

-- ============================================================
-- Students (50 in S3 Blue)
-- Admission numbers: STU001 - STU050
-- ============================================================
INSERT INTO students (id, school_id, admission_number, full_name, gender, status, is_active) VALUES
    (1,  1, 'STU001', 'Alice Nakato',      'FEMALE', 'ACTIVE', TRUE),
    (2,  1, 'STU002', 'Brian Okello',      'MALE',   'ACTIVE', TRUE),
    (3,  1, 'STU003', 'Carol Auma',        'FEMALE', 'ACTIVE', TRUE),
    (4,  1, 'STU004', 'David Mugisha',     'MALE',   'ACTIVE', TRUE),
    (5,  1, 'STU005', 'Esther Namuli',     'FEMALE', 'ACTIVE', TRUE),
    (6,  1, 'STU006', 'Francis Kato',      'MALE',   'ACTIVE', TRUE),
    (7,  1, 'STU007', 'Grace Akello',      'FEMALE', 'ACTIVE', TRUE),
    (8,  1, 'STU008', 'Henry Tumusiime',   'MALE',   'ACTIVE', TRUE),
    (9,  1, 'STU009', 'Irene Nabirye',     'FEMALE', 'ACTIVE', TRUE),
    (10, 1, 'STU010', 'James Ssemakula',   'MALE',   'ACTIVE', TRUE),
    (11, 1, 'STU011', 'Joan Kirabo',       'FEMALE', 'ACTIVE', TRUE),
    (12, 1, 'STU012', 'Kevin Ochieng',     'MALE',   'ACTIVE', TRUE),
    (13, 1, 'STU013', 'Linda Atim',        'FEMALE', 'ACTIVE', TRUE),
    (14, 1, 'STU014', 'Martin Byaruhanga', 'MALE',   'ACTIVE', TRUE),
    (15, 1, 'STU015', 'Nancy Adongo',      'FEMALE', 'ACTIVE', TRUE),
    (16, 1, 'STU016', 'Oscar Waiswa',      'MALE',   'ACTIVE', TRUE),
    (17, 1, 'STU017', 'Patricia Amuge',    'FEMALE', 'ACTIVE', TRUE),
    (18, 1, 'STU018', 'Quinton Lubega',    'MALE',   'ACTIVE', TRUE),
    (19, 1, 'STU019', 'Rebecca Nanteza',   'FEMALE', 'ACTIVE', TRUE),
    (20, 1, 'STU020', 'Samuel Kalema',     'MALE',   'ACTIVE', TRUE),
    (21, 1, 'STU021', 'Tracy Achieng',     'FEMALE', 'ACTIVE', TRUE),
    (22, 1, 'STU022', 'Umar Ssekandi',     'MALE',   'ACTIVE', TRUE),
    (23, 1, 'STU023', 'Vivian Nalwoga',    'FEMALE', 'ACTIVE', TRUE),
    (24, 1, 'STU024', 'Wilson Kiprotich',  'MALE',   'ACTIVE', TRUE),
    (25, 1, 'STU025', 'Xenia Nabukenya',   'FEMALE', 'ACTIVE', TRUE),
    (26, 1, 'STU026', 'Yusuf Mubiru',      'MALE',   'ACTIVE', TRUE),
    (27, 1, 'STU027', 'Zainab Nakimuli',   'FEMALE', 'ACTIVE', TRUE),
    (28, 1, 'STU028', 'Aaron Kigongo',     'MALE',   'ACTIVE', TRUE),
    (29, 1, 'STU029', 'Betty Namusoke',    'FEMALE', 'ACTIVE', TRUE),
    (30, 1, 'STU030', 'Charles Ojok',      'MALE',   'ACTIVE', TRUE),
    (31, 1, 'STU031', 'Diana Akumu',       'FEMALE', 'ACTIVE', TRUE),
    (32, 1, 'STU032', 'Edward Kizza',      'MALE',   'ACTIVE', TRUE),
    (33, 1, 'STU033', 'Faith Nabatanzi',   'FEMALE', 'ACTIVE', TRUE),
    (34, 1, 'STU034', 'George Otim',       'MALE',   'ACTIVE', TRUE),
    (35, 1, 'STU035', 'Hellen Amongi',     'FEMALE', 'ACTIVE', TRUE),
    (36, 1, 'STU036', 'Isaac Kamya',       'MALE',   'ACTIVE', TRUE),
    (37, 1, 'STU037', 'Jackie Nassiwa',    'FEMALE', 'ACTIVE', TRUE),
    (38, 1, 'STU038', 'Kenneth Obua',      'MALE',   'ACTIVE', TRUE),
    (39, 1, 'STU039', 'Lydia Atuhaire',    'FEMALE', 'ACTIVE', TRUE),
    (40, 1, 'STU040', 'Michael Ocen',      'MALE',   'ACTIVE', TRUE),
    (41, 1, 'STU041', 'Nora Nakabugo',     'FEMALE', 'ACTIVE', TRUE),
    (42, 1, 'STU042', 'Patrick Emojong',   'MALE',   'ACTIVE', TRUE),
    (43, 1, 'STU043', 'Queen Nabirye',     'FEMALE', 'ACTIVE', TRUE),
    (44, 1, 'STU044', 'Robert Aliro',      'MALE',   'ACTIVE', TRUE),
    (45, 1, 'STU045', 'Sarah Nakamya',     'FEMALE', 'ACTIVE', TRUE),
    (46, 1, 'STU046', 'Tom Kigongo',       'MALE',   'ACTIVE', TRUE),
    (47, 1, 'STU047', 'Ursula Nabwire',    'FEMALE', 'ACTIVE', TRUE),
    (48, 1, 'STU048', 'Victor Okiror',     'MALE',   'ACTIVE', TRUE),
    (49, 1, 'STU049', 'Winnie Nambooze',   'FEMALE', 'ACTIVE', TRUE),
    (50, 1, 'STU050', 'Xavier Kaggwa',     'MALE',   'ACTIVE', TRUE);


    
-- ============================================================
-- Classes (S3 and S4 in 2026, using class_levels 3 and 4)
-- Admin (user 1) is the class teacher for S3
-- ============================================================
INSERT INTO classes (id, school_id, class_level_id, academic_year_id, name, class_teacher_id, is_active) VALUES
    (1, 1, 3, 3, 'S3', 1, TRUE),
    (2, 1, 4, 3, 'S4', 1, TRUE);

-- ============================================================
-- Streams (S3 Blue, S3 Red, S4 Blue)
-- ============================================================
INSERT INTO streams (id, school_id, class_id, name, capacity, is_active) VALUES
    (1, 1, 1, 'Blue', 60, TRUE),
    (2, 1, 1, 'Red',  60, TRUE),
    (3, 1, 2, 'Blue', 60, TRUE);

-- ============================================================
-- Assign students to current class/stream
-- Students 1-30 → S3 Blue (stream 1)
-- Students 31-50 → S3 Red (stream 2)
-- ============================================================
UPDATE students SET current_class_id = 1, current_stream_id = 1 WHERE id BETWEEN 1 AND 30;
UPDATE students SET current_class_id = 1, current_stream_id = 2 WHERE id BETWEEN 31 AND 50;






-- ============================================================
-- Subjects (5 core subjects)
-- ============================================================
INSERT INTO subjects (id, school_id, code, name, description, is_core, is_active) VALUES
    (1, 1, 'MATH', 'Mathematics', 'Core mathematics', TRUE,  TRUE),
    (2, 1, 'ENG',  'English',     'English language', TRUE,  TRUE),
    (3, 1, 'BIO',  'Biology',     'Life sciences',    TRUE,  TRUE),
    (4, 1, 'CHEM', 'Chemistry',   'Chemical sciences', TRUE,  TRUE),
    (5, 1, 'PHY',  'Physics',     'Physical sciences', TRUE,  TRUE);

-- ============================================================
-- Topics (4 per subject = 20 total)
-- ============================================================
INSERT INTO topics (id, school_id, subject_id, name, sort_order) VALUES
    -- Mathematics (subject 1)
    (1,  1, 1, 'Algebra',            1),
    (2,  1, 1, 'Geometry',           2),
    (3,  1, 1, 'Trigonometry',       3),
    (4,  1, 1, 'Statistics',         4),
    -- English (subject 2)
    (5,  1, 2, 'Comprehension',      1),
    (6,  1, 2, 'Grammar',            2),
    (7,  1, 2, 'Essay Writing',      3),
    (8,  1, 2, 'Literature',         4),
    -- Biology (subject 3)
    (9,  1, 3, 'Cell Biology',       1),
    (10, 1, 3, 'Genetics',           2),
    (11, 1, 3, 'Ecology',            3),
    (12, 1, 3, 'Human Anatomy',      4),
    -- Chemistry (subject 4)
    (13, 1, 4, 'Atomic Structure',   1),
    (14, 1, 4, 'Chemical Bonding',   2),
    (15, 1, 4, 'Acids and Bases',    3),
    (16, 1, 4, 'Organic Chemistry',  4),
    -- Physics (subject 5)
    (17, 1, 5, 'Mechanics',          1),
    (18, 1, 5, 'Electricity',        2),
    (19, 1, 5, 'Waves',              3),
    (20, 1, 5, 'Thermodynamics',     4);

-- ============================================================
-- Class Subjects (which subjects are offered at which class levels)
-- S3 and S4 offer all 5 subjects
-- ============================================================
INSERT INTO class_subjects (id, school_id, class_level_id, subject_id) VALUES
    (1,  1, 3, 1), (2,  1, 3, 2), (3,  1, 3, 3), (4,  1, 3, 4), (5,  1, 3, 5),
    (6,  1, 4, 1), (7,  1, 4, 2), (8,  1, 4, 3), (9,  1, 4, 4), (10, 1, 4, 5);

-- ============================================================
-- Teacher Assignments (teacher1 and teacher2 assigned to subjects)
-- teacher1 (user 2) → Math, Physics
-- teacher2 (user 3) → English, Biology
-- ============================================================
INSERT INTO teacher_assignments (id, school_id, teacher_id, subject_id, class_id, stream_id, academic_year_id) VALUES
    -- teacher1 → S3 Math, S3 Physics
    (1, 1, 2, 1, 1, NULL, 3),
    (2, 1, 2, 5, 1, NULL, 3),
    -- teacher2 → S3 English, S3 Biology
    (3, 1, 3, 2, 1, NULL, 3),
    (4, 1, 3, 3, 1, NULL, 3),
    -- teacher1 → S4 Math
    (5, 1, 2, 1, 2, NULL, 3);



    
-- ============================================================
-- Assessment (1 published test in S3 Math, Term 1 2026)
-- ============================================================
INSERT INTO assessments (id, school_id, teacher_id, class_id, stream_id, subject_id, term_id,
                         title, type, assessment_date, max_score, status, published_at, published_by) VALUES
    (1, 1, 2, 1, NULL, 1, 7,
     'Term 1 Math Test', 'TEST', '2026-03-15', 100.00, 'PUBLISHED', NOW(), 1);

-- Tag the assessment with 2 topics (Algebra, Geometry)
INSERT INTO assessment_topics (id, school_id, assessment_id, topic_id) VALUES
    (1, 1, 1, 1),
    (2, 1, 1, 2);

-- ============================================================
-- Scores (all 50 students get a score on the Math test)
-- Scores vary from 45 to 98 for realistic spread
-- ============================================================
INSERT INTO scores (id, school_id, assessment_id, student_id, score, entered_by, entered_at) VALUES
    (1,  1, 1, 1,  85.00, 2, NOW()),
    (2,  1, 1, 2,  72.50, 2, NOW()),
    (3,  1, 1, 3,  91.00, 2, NOW()),
    (4,  1, 1, 4,  68.00, 2, NOW()),
    (5,  1, 1, 5,  78.50, 2, NOW()),
    (6,  1, 1, 6,  55.00, 2, NOW()),
    (7,  1, 1, 7,  88.00, 2, NOW()),
    (8,  1, 1, 8,  63.50, 2, NOW()),
    (9,  1, 1, 9,  95.00, 2, NOW()),
    (10, 1, 1, 10, 47.00, 2, NOW()),
    (11, 1, 1, 11, 82.00, 2, NOW()),
    (12, 1, 1, 12, 76.50, 2, NOW()),
    (13, 1, 1, 13, 89.00, 2, NOW()),
    (14, 1, 1, 14, 58.00, 2, NOW()),
    (15, 1, 1, 15, 74.00, 2, NOW()),
    (16, 1, 1, 16, 66.50, 2, NOW()),
    (17, 1, 1, 17, 92.50, 2, NOW()),
    (18, 1, 1, 18, 49.00, 2, NOW()),
    (19, 1, 1, 19, 80.00, 2, NOW()),
    (20, 1, 1, 20, 71.00, 2, NOW()),
    (21, 1, 1, 21, 87.50, 2, NOW()),
    (22, 1, 1, 22, 54.00, 2, NOW()),
    (23, 1, 1, 23, 90.00, 2, NOW()),
    (24, 1, 1, 24, 65.00, 2, NOW()),
    (25, 1, 1, 25, 77.00, 2, NOW()),
    (26, 1, 1, 26, 83.50, 2, NOW()),
    (27, 1, 1, 27, 45.50, 2, NOW()),
    (28, 1, 1, 28, 93.00, 2, NOW()),
    (29, 1, 1, 29, 69.50, 2, NOW()),
    (30, 1, 1, 30, 79.00, 2, NOW()),
    (31, 1, 1, 31, 61.00, 2, NOW()),
    (32, 1, 1, 32, 86.00, 2, NOW()),
    (33, 1, 1, 33, 52.50, 2, NOW()),
    (34, 1, 1, 34, 73.00, 2, NOW()),
    (35, 1, 1, 35, 98.00, 2, NOW()),
    (36, 1, 1, 36, 57.50, 2, NOW()),
    (37, 1, 1, 37, 84.50, 2, NOW()),
    (38, 1, 1, 38, 67.00, 2, NOW()),
    (39, 1, 1, 39, 75.50, 2, NOW()),
    (40, 1, 1, 40, 51.00, 2, NOW()),
    (41, 1, 1, 41, 81.50, 2, NOW()),
    (42, 1, 1, 42, 62.00, 2, NOW()),
    (43, 1, 1, 43, 94.50, 2, NOW()),
    (44, 1, 1, 44, 59.00, 2, NOW()),
    (45, 1, 1, 45, 70.50, 2, NOW()),
    (46, 1, 1, 46, 64.50, 2, NOW()),
    (47, 1, 1, 47, 88.50, 2, NOW()),
    (48, 1, 1, 48, 46.00, 2, NOW()),
    (49, 1, 1, 49, 76.00, 2, NOW()),
    (50, 1, 1, 50, 82.50, 2, NOW());

-- ============================================================
-- School Settings (grading scale, default term)
-- ============================================================
INSERT INTO school_settings (id, school_id, grading_scale, current_term_id) VALUES
    (1, 1,
     '[{"grade":"A","min":80,"max":100},
       {"grade":"B","min":70,"max":79},
       {"grade":"C","min":60,"max":69},
       {"grade":"D","min":50,"max":59},
       {"grade":"F","min":0,"max":49}]'::jsonb,
     7);

-- ============================================================
-- Reset sequences so future inserts don't collide with seed IDs
-- ============================================================
SELECT setval('schools_id_seq',            (SELECT MAX(id) FROM schools));
SELECT setval('academic_years_id_seq',     (SELECT MAX(id) FROM academic_years));
SELECT setval('terms_id_seq',              (SELECT MAX(id) FROM terms));
SELECT setval('class_levels_id_seq',       (SELECT MAX(id) FROM class_levels));
SELECT setval('classes_id_seq',            (SELECT MAX(id) FROM classes));
SELECT setval('streams_id_seq',            (SELECT MAX(id) FROM streams));
SELECT setval('users_id_seq',              (SELECT MAX(id) FROM users));
SELECT setval('students_id_seq',           (SELECT MAX(id) FROM students));
SELECT setval('subjects_id_seq',           (SELECT MAX(id) FROM subjects));
SELECT setval('topics_id_seq',             (SELECT MAX(id) FROM topics));
SELECT setval('class_subjects_id_seq',     (SELECT MAX(id) FROM class_subjects));
SELECT setval('teacher_assignments_id_seq',(SELECT MAX(id) FROM teacher_assignments));
SELECT setval('assessments_id_seq',        (SELECT MAX(id) FROM assessments));
SELECT setval('assessment_topics_id_seq',  (SELECT MAX(id) FROM assessment_topics));
SELECT setval('scores_id_seq',             (SELECT MAX(id) FROM scores));
SELECT setval('school_settings_id_seq',    (SELECT MAX(id) FROM school_settings));