-- V001__create_schools.sql
-- Creates the schools table. First table in the system.

CREATE TABLE schools (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    logo_url VARCHAR(500),
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);