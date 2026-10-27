-- CampusBrain MySQL Database Initialization Script
-- Target Database: campusbrain
-- Target Engine: InnoDB, Charset: utf8mb4

CREATE DATABASE IF NOT EXISTS campusbrain CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campusbrain;

-- Admin Users Table
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Academic Materials Table
CREATE TABLE IF NOT EXISTS academic_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    academic_year VARCHAR(50) NOT NULL,
    semester VARCHAR(50) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    file_format VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_filename VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_published (category, published),
    INDEX idx_subject (subject),
    INDEX idx_year_semester (academic_year, semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
