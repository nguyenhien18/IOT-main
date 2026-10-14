-- =========================================
-- 1. DATABASE
-- =========================================

CREATE DATABASE IF NOT EXISTS iot_smart_room1
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE iot_smart_room1;

-- =========================================
-- 2. USERS
-- =========================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    student_code VARCHAR(30) NOT NULL,
    class_name VARCHAR(50) NOT NULL,
    avatar_url LONGTEXT,
    email VARCHAR(100),
    github_url VARCHAR(255),
    figma_url VARCHAR(255),
    postman_url VARCHAR(255),
    report_url VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- 3. SENSORS
-- =========================================

CREATE TABLE IF NOT EXISTS sensors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================
-- 4. DATA_SENSORS
-- =========================================

CREATE TABLE IF NOT EXISTS data_sensors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id BIGINT NOT NULL,
    `value` DOUBLE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_data_sensor_sensor
        FOREIGN KEY (sensor_id)
        REFERENCES sensors(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_data_sensor_sensor_created_at (sensor_id, created_at)
);

-- =========================================
-- 5. DEVICES
-- =========================================

CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'OFF',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_device_status
        CHECK (status IN ('ON', 'OFF'))
);

-- =========================================
-- 6. ACTIONS
-- =========================================

CREATE TABLE IF NOT EXISTS actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_action_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_action_device
        FOREIGN KEY (device_id)
        REFERENCES devices(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_action
        CHECK (action IN ('ON', 'OFF')),

    CONSTRAINT chk_action_status
        CHECK (status IN ('PROCESSING', 'SUCCESS', 'FAILED')),

    INDEX idx_action_user_created_at (user_id, created_at),
    INDEX idx_action_device_created_at (device_id, created_at),
    INDEX idx_action_status (status)
);

-- =========================================
-- 7. INITIAL DATA
-- =========================================

INSERT IGNORE INTO sensors (name)
VALUES ('TEMPERATURE'), ('HUMIDITY'), ('LIGHT');

INSERT IGNORE INTO devices (name, status)
VALUES ('LED 1', 'OFF'), ('LED 2', 'OFF'), ('LED 3', 'OFF');
