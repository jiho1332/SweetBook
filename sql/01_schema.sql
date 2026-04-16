CREATE DATABASE IF NOT EXISTS sweetbook_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sweetbook_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS sweetbook_api_request;
DROP TABLE IF EXISTS book_order;
DROP TABLE IF EXISTS book_project;
DROP TABLE IF EXISTS memory;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS member;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE member (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id),
    UNIQUE KEY uk_member_email (email)
);

CREATE TABLE pet (
    pet_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(50) NULL,
    breed VARCHAR(100) NULL,
    relationship_label VARCHAR(30) NULL,
    memorial_date DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pet_token VARCHAR(100) NOT NULL COMMENT '반려견 내부 고유 토큰',
    profile_image_url VARCHAR(500) NULL COMMENT '대표 이미지 경로',
    PRIMARY KEY (pet_id),
    CONSTRAINT uk_pet_token UNIQUE (pet_token),
    CONSTRAINT fk_pet_member
        FOREIGN KEY (member_id)
        REFERENCES member (member_id)
);

CREATE TABLE memory (
    memory_id BIGINT NOT NULL AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    chapter_type VARCHAR(20) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500) NULL,
    is_representative CHAR(1) NOT NULL DEFAULT 'N',
    recorded_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (memory_id),
    KEY idx_memory_pet_id (pet_id),
    KEY idx_memory_chapter_type (chapter_type),
    CONSTRAINT fk_memory_pet
        FOREIGN KEY (pet_id)
        REFERENCES pet (pet_id),
    CONSTRAINT chk_memory_chapter_type
        CHECK (chapter_type IN ('INTRO', 'DAILY', 'ILLNESS', 'FAREWELL', 'AFTER'))
);

CREATE TABLE book_project (
    book_project_id BIGINT NOT NULL AUTO_INCREMENT,
    pet_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    cover_title VARCHAR(200) NULL,
    cover_subtitle VARCHAR(200) NULL,
    dedication_text TEXT NULL,
    template_code VARCHAR(100) NULL,
    book_spec_code VARCHAR(100) NULL,
    book_spec_uid VARCHAR(100) NULL,
    cover_template_uid VARCHAR(100) NULL,
    content_template_uid VARCHAR(100) NULL,
    sweetbook_book_id VARCHAR(100) NULL,
    book_uid VARCHAR(100) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    content_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at DATETIME NULL,
    PRIMARY KEY (book_project_id),
    KEY idx_book_project_pet_id (pet_id),
    KEY idx_book_project_status (status),
    KEY idx_book_project_book_spec_uid (book_spec_uid),
    KEY idx_book_project_cover_template_uid (cover_template_uid),
    KEY idx_book_project_content_template_uid (content_template_uid),
    KEY idx_book_project_book_uid (book_uid),
    CONSTRAINT fk_book_project_pet
        FOREIGN KEY (pet_id)
        REFERENCES pet (pet_id)
);

CREATE TABLE book_order (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    book_project_id BIGINT NOT NULL,
    order_number VARCHAR(100) NOT NULL,
    sweetbook_order_id VARCHAR(100) NULL,
    total_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estimated_price DECIMAL(10,2) NULL,
    status VARCHAR(30) NOT NULL,
    recipient_name VARCHAR(100) NULL,
    recipient_phone VARCHAR(30) NULL,
    address VARCHAR(300) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ordered_at DATETIME NULL,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_book_order_order_number (order_number),
    KEY idx_book_order_member_id (member_id),
    KEY idx_book_order_book_project_id (book_project_id),
    CONSTRAINT fk_book_order_member
        FOREIGN KEY (member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_book_order_book_project
        FOREIGN KEY (book_project_id)
        REFERENCES book_project (book_project_id)
);

CREATE TABLE sweetbook_api_request (
    api_request_id BIGINT NOT NULL AUTO_INCREMENT,
    book_project_id BIGINT NULL,
    order_id BIGINT NULL,
    api_type VARCHAR(50) NOT NULL,
    request_payload TEXT NULL,
    response_payload TEXT NULL,
    external_resource_id VARCHAR(100) NULL,
    result_code VARCHAR(50) NULL,
    result_message VARCHAR(255) NULL,
    requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (api_request_id),
    KEY idx_sweetbook_api_request_book_project_id (book_project_id),
    KEY idx_sweetbook_api_request_order_id (order_id),
    CONSTRAINT fk_sweetbook_api_request_book_project
        FOREIGN KEY (book_project_id)
        REFERENCES book_project (book_project_id),
    CONSTRAINT fk_sweetbook_api_request_book_order
        FOREIGN KEY (order_id)
        REFERENCES book_order (order_id)
);

COMMIT;
