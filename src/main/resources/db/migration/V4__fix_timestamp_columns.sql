-- V4: Fix timestamp column names to match JPA entity fields
-- Rename create_at to created_at for users and posts tables

-- Users table
ALTER TABLE users
  CHANGE COLUMN create_at created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Posts table
ALTER TABLE posts
  CHANGE COLUMN create_at created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

