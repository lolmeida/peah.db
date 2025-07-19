-- V1.0.0__Create_initial_schema.sql
-- Initial database schema migration

-- Create a sample table to demonstrate the migration structure
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create an index on email for faster lookups
CREATE INDEX idx_users_email ON users(email);

-- Create an index on username for faster lookups
CREATE INDEX idx_users_username ON users(username);

-- Create a sample configuration table
CREATE TABLE IF NOT EXISTS app_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default configuration values for MySQL compatibility
INSERT INTO app_config (config_key, config_value, description) VALUES
    ('app.name', 'PeahDB', 'Application name'),
    ('app.version', '1.0.0', 'Application version'),
    ('app.environment', 'development', 'Current environment')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description); 