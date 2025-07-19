-- Dummy data for User entity
-- Note: Password hashes are dummy bcrypt-like hashes for testing purposes only

INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES
('john_doe', 'john.doe@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '2023-01-15 10:30:00', '2023-01-15 10:30:00'),
('jane_smith', 'jane.smith@email.com', '$2a$10$DOwJHjcCaLNQqRXdP0WFP.HuJvxQA5zJNOOWwgTSzQFkUWyVpwDl6', '2023-02-20 14:15:30', '2023-02-25 16:45:20'),
('bob_wilson', 'bob.wilson@email.com', '$2a$10$8K.9S9jVgCcG0qFUKtRkFe5lJuOKwmUkfCwGCqSNGhEZjSHQO5LOS', '2023-03-10 09:20:45', '2023-03-10 09:20:45'),
('alice_johnson', 'alice.johnson@email.com', '$2a$10$7hGPQXfTJw8bCDDVfKxO2OYdmW9uULPNVNXPOmk2eOWQCOzJ5RWiG', '2023-04-05 11:55:12', '2023-04-12 13:22:35'),
('charlie_brown', 'charlie.brown@email.com', '$2a$10$RZKCfQRUvQXDJFgvLJn3nOdXlMZhZmGJfNdKnMGYfGTfVGLsJZwPW', '2023-05-18 08:40:25', '2023-05-18 08:40:25'),
('diana_prince', 'diana.prince@email.com', '$2a$10$LmNpQfRHvJdWjFgKGdnT2OzUKJGJGYLPzGJKhMoWKqNMXOJfQdSJK', '2023-06-02 15:30:00', '2023-06-08 10:15:45'),
('test_user', 'test@example.com', '$2a$10$MnVpKfLGvJsWdFaKPqnF5OuUGJnLGYJPnGJMhVoTMnLMXKJfPsSKR', '2023-07-01 12:00:00', '2023-07-01 12:00:00'),
('admin_user', 'admin@peahdb.com', '$2a$10$PzXpLfJGvGdWjKaKJqnD6OyUHJoLGZJPoGJNhWoUNoLNXLJfQdTJL', '2023-08-15 09:45:30', '2023-08-20 14:30:15');


-- App Manifests Seed Data
-- Defines which Kubernetes manifests each app requires
-- Based on current templates and helper configurations

-- ============================================================================
-- 1. APPS DEFINITION
-- ============================================================================

-- Database Stack Apps
INSERT INTO config_apps (name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, health_check_path, default_ports, default_resources, dependencies) VALUES
('postgresql', 'PostgreSQL Database', 'PostgreSQL relational database', 'database', 'postgres', '14', 10, NULL,
 '{"postgres": 5432}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),

('mysql', 'MySQL Database', 'MySQL relational database', 'database', 'mysql', '8.0', 10, NULL,
 '{"mysql": 3306}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),

('redis', 'Redis Cache', 'Redis in-memory data store', 'database', 'redis', '7-alpine', 10, NULL,
 '{"redis": 6379}',
 '{"limits": {"memory": "256Mi", "cpu": "100m"}, "requests": {"memory": "128Mi", "cpu": "50m"}}',
 '[]');

-- Monitoring Stack Apps
INSERT INTO config_apps (name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, health_check_path, default_ports, default_resources, dependencies) VALUES
('prometheus', 'Prometheus Monitoring', 'Prometheus metrics collection and monitoring', 'monitoring', 'prom/prometheus', 'v2.45.0', 20, '/healthy',
 '{"http": 9090}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),

('grafana', 'Grafana Dashboard', 'Grafana visualization and dashboards', 'monitoring', 'grafana/grafana', '10.2.0', 30, '/api/health',
 '{"http": 3000}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '["prometheus"]');

-- Application Stack Apps
INSERT INTO config_apps (name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, health_check_path, readiness_check_path, default_ports, default_resources, dependencies) VALUES
('n8n', 'N8N Automation', 'N8N workflow automation platform', 'automation', 'n8nio/n8n', 'latest', 50, '/healthz', '/healthz',
 '{"http": 5678}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "50m"}}',
 '["postgresql", "redis"]'),

('peahdb', 'Peah-DB Logistics API', 'Quarkus-based logistics API', 'api', 'lolmeida/peah-db', 'latest', 60, '/q/health/live', '/q/health/ready',
 '{"http": 8080}',
 '{"limits": {"memory": "512Mi", "cpu": "500m"}, "requests": {"memory": "256Mi", "cpu": "250m"}}',
 '["postgresql"]');

-- ============================================================================
-- 2. APP MANIFEST DEFINITIONS
-- ============================================================================

-- PostgreSQL Manifests (Priority Database)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'DEPLOYMENT', true, 1, 'PostgreSQL deployment with persistent storage', NULL),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'SERVICE', true, 2, 'PostgreSQL service for internal access', NULL),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'PERSISTENT_VOLUME_CLAIM', true, 3, 'Persistent storage for PostgreSQL data', 'persistence.enabled'),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'SECRET', true, 4, 'PostgreSQL passwords and credentials', NULL),
-- Optional manifests
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'HPA', false, 90, 'Auto-scaling for PostgreSQL (not recommended)', 'hpa.enabled');

-- MySQL Manifests
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'mysql'), 'DEPLOYMENT', true, 1, 'MySQL deployment with persistent storage', NULL),
((SELECT id FROM config_apps WHERE name = 'mysql'), 'SERVICE', true, 2, 'MySQL service for internal access', NULL),
((SELECT id FROM config_apps WHERE name = 'mysql'), 'PERSISTENT_VOLUME_CLAIM', true, 3, 'Persistent storage for MySQL data', 'persistence.enabled'),
((SELECT id FROM config_apps WHERE name = 'mysql'), 'SECRET', true, 4, 'MySQL passwords and credentials', NULL),
-- Optional manifests
((SELECT id FROM config_apps WHERE name = 'mysql'), 'HPA', false, 90, 'Auto-scaling for MySQL (not recommended)', 'hpa.enabled');

-- Redis Manifests
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'redis'), 'DEPLOYMENT', true, 1, 'Redis deployment', NULL),
((SELECT id FROM config_apps WHERE name = 'redis'), 'SERVICE', true, 2, 'Redis service for internal access', NULL),
((SELECT id FROM config_apps WHERE name = 'redis'), 'SECRET', true, 3, 'Redis authentication password', NULL),
-- Optional manifests
((SELECT id FROM config_apps WHERE name = 'redis'), 'PERSISTENT_VOLUME_CLAIM', false, 40, 'Persistent storage for Redis (optional)', 'persistence.enabled'),
((SELECT id FROM config_apps WHERE name = 'redis'), 'HPA', false, 90, 'Auto-scaling for Redis', 'hpa.enabled');

-- Prometheus Manifests (Complex - needs RBAC)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Security first
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'SERVICE_ACCOUNT', true, 1, 'Service account for Prometheus', NULL),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'CLUSTER_ROLE', true, 2, 'Cluster role for metrics scraping', NULL),
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'CONFIG_MAP', true, 5, 'Prometheus configuration', NULL),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'DEPLOYMENT', true, 10, 'Prometheus deployment', NULL),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'SERVICE', true, 11, 'Prometheus service', NULL),
-- Storage and access
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'PERSISTENT_VOLUME_CLAIM', true, 12, 'Persistent storage for Prometheus data', 'persistence.enabled'),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'INGRESS', false, 80, 'External access to Prometheus UI', 'ingress.enabled');

-- Grafana Manifests
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'grafana'), 'DEPLOYMENT', true, 10, 'Grafana deployment', NULL),
((SELECT id FROM config_apps WHERE name = 'grafana'), 'SERVICE', true, 11, 'Grafana service', NULL),
((SELECT id FROM config_apps WHERE name = 'grafana'), 'SECRET', true, 12, 'Grafana admin credentials', NULL),
-- Configuration and storage
((SELECT id FROM config_apps WHERE name = 'grafana'), 'CONFIG_MAP', false, 15, 'Grafana configuration and dashboards', 'configMap.enabled'),
((SELECT id FROM config_apps WHERE name = 'grafana'), 'PERSISTENT_VOLUME_CLAIM', false, 20, 'Persistent storage for Grafana data', 'persistence.enabled'),
-- External access
((SELECT id FROM config_apps WHERE name = 'grafana'), 'INGRESS', true, 80, 'External access to Grafana dashboard', 'ingress.enabled');

-- N8N Manifests (Most Complex - depends on database + cache)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'n8n'), 'DEPLOYMENT', true, 50, 'N8N automation platform deployment', NULL),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'SERVICE', true, 51, 'N8N service', NULL),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'SECRET', true, 52, 'N8N authentication and database credentials', NULL),
-- Storage and access
((SELECT id FROM config_apps WHERE name = 'n8n'), 'PERSISTENT_VOLUME_CLAIM', true, 53, 'Persistent storage for N8N workflows', 'persistence.enabled'),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'INGRESS', true, 80, 'External access to N8N interface', 'ingress.enabled'),
-- Scaling
((SELECT id FROM config_apps WHERE name = 'n8n'), 'HPA', false, 90, 'Auto-scaling for N8N', 'hpa.enabled');

-- Peah-DB Manifests (Quarkus API)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description, creation_condition) VALUES
-- Core manifests
((SELECT id FROM config_apps WHERE name = 'peahdb'), 'DEPLOYMENT', true, 60, 'Peah-DB logistics API deployment', NULL),
((SELECT id FROM config_apps WHERE name = 'peahdb'), 'SERVICE', true, 61, 'Peah-DB service', NULL),
-- External access
((SELECT id FROM config_apps WHERE name = 'peahdb'), 'INGRESS', true, 80, 'External access to Peah-DB API', 'ingress.enabled'),
-- Scaling
((SELECT id FROM config_apps WHERE name = 'peahdb'), 'HPA', true, 85, 'Auto-scaling for Peah-DB API', NULL);




-- Criar ambientes
INSERT INTO config_environments (name, description, id) VALUES
('prod', 'Production environment',1),
('staging', 'Staging environment',2),
('dev', 'Development environment',3);
('dev', 'Development environment',4);

-- Criar stacks para produção
INSERT INTO config_stacks (environment_id, name, enabled, description) VALUES 
(1, 'database', true, 'Database services stack'),
(1, 'monitoring', true, 'Monitoring and observability stack'),
(1, 'apps', true, 'Applications stack');

-- ====== APPS AND MANIFESTS CONFIGURATION ======

-- Database Stack Apps (Priority 10)
INSERT INTO config_apps (stack_id, name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, enabled, default_ports, default_resources, dependencies) VALUES 
-- Database stack
(1, 'postgresql', 'PostgreSQL Database', 'PostgreSQL relational database', 'database', 'postgres', '14', 10, true, 
 '{"postgres": 5432}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),
(1, 'mysql', 'MySQL Database', 'MySQL relational database', 'database', 'mysql', '8.0', 10, true,
 '{"mysql": 3306}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),
(1, 'redis', 'Redis Cache', 'Redis in-memory data store', 'database', 'redis', '7-alpine', 10, true,
 '{"redis": 6379}',
 '{"limits": {"memory": "256Mi", "cpu": "100m"}, "requests": {"memory": "128Mi", "cpu": "50m"}}',
 '[]');

-- Monitoring Stack Apps (Priority 20-30)  
INSERT INTO config_apps (stack_id, name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, health_check_path, enabled, default_ports, default_resources, dependencies) VALUES
(2, 'prometheus', 'Prometheus Monitoring', 'Prometheus metrics collection', 'monitoring', 'prom/prometheus', 'v2.45.0', 20, '/healthy', true,
 '{"http": 9090}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '[]'),
(2, 'grafana', 'Grafana Dashboard', 'Grafana visualization dashboards', 'monitoring', 'grafana/grafana', '10.2.0', 30, '/api/health', true,
 '{"http": 3000}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}',
 '["prometheus"]');

-- Application Stack Apps (Priority 50-60)
INSERT INTO config_apps (stack_id, name, display_name, description, category, default_image_repository, default_image_tag, deployment_priority, health_check_path, readiness_check_path, enabled, default_ports, default_resources, dependencies) VALUES
(3, 'n8n', 'N8N Automation', 'N8N workflow automation platform', 'automation', 'n8nio/n8n', 'latest', 50, '/healthz', '/healthz', true,
 '{"http": 5678}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "50m"}}',
 '["postgresql", "redis"]'),
(3, 'peahdb', 'Peah-DB Logistics API', 'Quarkus-based logistics API', 'api', 'lolmeida/peah-db', 'latest', 60, '/q/health/live', '/q/health/ready', true,
 '{"http": 8080}',
 '{"limits": {"memory": "512Mi", "cpu": "500m"}, "requests": {"memory": "256Mi", "cpu": "250m"}}',
 '["postgresql"]');

-- ====== APP MANIFEST DEFINITIONS ======

-- PostgreSQL Manifests (Database priority)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'DEPLOYMENT', true, 1, 'PostgreSQL deployment with persistent storage'),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'SERVICE', true, 2, 'PostgreSQL service for internal access'),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'PERSISTENT_VOLUME_CLAIM', true, 3, 'Persistent storage for PostgreSQL data'),
((SELECT id FROM config_apps WHERE name = 'postgresql'), 'SECRET', true, 4, 'PostgreSQL passwords and credentials');

-- Redis Manifests  
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
((SELECT id FROM config_apps WHERE name = 'redis'), 'DEPLOYMENT', true, 1, 'Redis deployment'),
((SELECT id FROM config_apps WHERE name = 'redis'), 'SERVICE', true, 2, 'Redis service for internal access'), 
((SELECT id FROM config_apps WHERE name = 'redis'), 'SECRET', true, 3, 'Redis authentication password');

-- Prometheus Manifests (Complex - needs RBAC)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'SERVICE_ACCOUNT', true, 1, 'Service account for Prometheus'),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'CLUSTER_ROLE', true, 2, 'Cluster role for metrics scraping'),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'CONFIG_MAP', true, 5, 'Prometheus configuration'),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'DEPLOYMENT', true, 10, 'Prometheus deployment'),
((SELECT id FROM config_apps WHERE name = 'prometheus'), 'SERVICE', true, 11, 'Prometheus service');

-- N8N Manifests (Complex - depends on database)
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
((SELECT id FROM config_apps WHERE name = 'n8n'), 'DEPLOYMENT', true, 50, 'N8N automation platform'),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'SERVICE', true, 51, 'N8N service'),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'SECRET', true, 52, 'N8N authentication credentials'),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'PERSISTENT_VOLUME_CLAIM', true, 53, 'N8N workflow storage'),
((SELECT id FROM config_apps WHERE name = 'n8n'), 'INGRESS', true, 80, 'N8N external access');

-- ============================================================================
-- 3. VERIFICATION QUERIES
-- ============================================================================

-- Show apps with their manifest counts
/*
SELECT
    a.name,
    a.display_name,
    a.category,
    a.deployment_priority,
    COUNT(am.id) as total_manifests,
    COUNT(CASE WHEN am.required = true THEN 1 END) as required_manifests,
    COUNT(CASE WHEN am.required = false THEN 1 END) as optional_manifests
FROM config_apps a
LEFT JOIN config_app_manifests am ON a.id = am.app_id
GROUP BY a.id, a.name, a.display_name, a.category, a.deployment_priority
ORDER BY a.deployment_priority, a.name;
*/

-- Show detailed manifest breakdown by app
/*
SELECT
    a.name as app_name,
    a.category,
    am.manifest_type,
    am.required,
    am.creation_priority,
    am.description,
    am.creation_condition
FROM config_apps a
JOIN config_app_manifests am ON a.id = am.app_id
ORDER BY a.deployment_priority, a.name, am.creation_priority;
*/

-- Show deployment order (by priority)
/*
SELECT
    a.deployment_priority,
    a.name,
    a.display_name,
    json_array_length(a.dependencies::json) as dependency_count,
    a.dependencies
FROM config_apps a
ORDER BY a.deployment_priority, a.name;
*/