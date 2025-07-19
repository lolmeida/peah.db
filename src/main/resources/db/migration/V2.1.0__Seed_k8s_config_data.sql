-- V2.1.0__Seed_k8s_config_data.sql
-- Seed data for K8s configuration

-- Insert environments
INSERT INTO config_environments (name, description, is_active) VALUES 
('prod', 'Production environment', true),
('staging', 'Staging environment', true),
('dev', 'Development environment', true);

-- Get environment IDs
SET @prod_env_id = (SELECT id FROM config_environments WHERE name = 'prod');
SET @staging_env_id = (SELECT id FROM config_environments WHERE name = 'staging');
SET @dev_env_id = (SELECT id FROM config_environments WHERE name = 'dev');

-- Insert stacks for production environment
INSERT INTO config_stacks (environment_id, name, enabled, description) VALUES 
(@prod_env_id, 'database', true, 'Database services stack'),
(@prod_env_id, 'monitoring', true, 'Monitoring and observability stack'),
(@prod_env_id, 'apps', false, 'Applications stack');

-- Get stack IDs
SET @prod_db_stack_id = (SELECT id FROM config_stacks WHERE environment_id = @prod_env_id AND name = 'database');
SET @prod_mon_stack_id = (SELECT id FROM config_stacks WHERE environment_id = @prod_env_id AND name = 'monitoring');
SET @prod_apps_stack_id = (SELECT id FROM config_stacks WHERE environment_id = @prod_env_id AND name = 'apps');

-- Insert database stack apps
INSERT INTO config_apps (stack_id, name, display_name, description, category, enabled, default_image_repository, default_image_tag, deployment_priority, health_check_path, default_config, dependencies, default_ports, default_resources) VALUES 
(@prod_db_stack_id, 'postgresql', 'PostgreSQL Database', 'PostgreSQL relational database', 'database', true, 'postgres', '14', 10, '/health',
 '{"persistence": {"enabled": true, "size": "10Gi"}}',
 '[]',
 '{"postgres": 5432}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}'),
 
(@prod_db_stack_id, 'redis', 'Redis Cache', 'Redis in-memory data store', 'database', true, 'redis', '7-alpine', 10, '/health',
 '{"auth": {"enabled": true}}',
 '[]',
 '{"redis": 6379}',
 '{"limits": {"memory": "256Mi", "cpu": "100m"}, "requests": {"memory": "128Mi", "cpu": "50m"}}');

-- Insert monitoring stack apps
INSERT INTO config_apps (stack_id, name, display_name, description, category, enabled, default_image_repository, default_image_tag, deployment_priority, health_check_path, default_config, dependencies, default_ports, default_resources) VALUES
(@prod_mon_stack_id, 'prometheus', 'Prometheus Monitoring', 'Prometheus metrics collection', 'monitoring', true, 'prom/prometheus', 'v2.45.0', 20, '/healthy',
 '{"retention": "30d", "scrapeInterval": "30s"}',
 '[]',
 '{"http": 9090}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}'),
 
(@prod_mon_stack_id, 'grafana', 'Grafana Dashboard', 'Grafana visualization dashboards', 'monitoring', false, 'grafana/grafana', '10.2.0', 30, '/api/health',
 '{"adminPassword": "admin", "persistence": {"enabled": true, "size": "1Gi"}}',
 '["prometheus"]',
 '{"http": 3000}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "100m"}}');

-- Insert application stack apps
INSERT INTO config_apps (stack_id, name, display_name, description, category, enabled, default_image_repository, default_image_tag, deployment_priority, health_check_path, readiness_check_path, default_config, dependencies, default_ports, default_resources) VALUES
(@prod_apps_stack_id, 'n8n', 'N8N Automation', 'N8N workflow automation platform', 'automation', false, 'n8nio/n8n', 'latest', 50, '/healthz', '/healthz',
 '{"timezone": "Europe/Lisbon", "encryption_key": "n8n-secret-key"}',
 '["postgresql", "redis"]',
 '{"http": 5678}',
 '{"limits": {"memory": "512Mi", "cpu": "200m"}, "requests": {"memory": "256Mi", "cpu": "50m"}}'),
 
(@prod_apps_stack_id, 'peahdb', 'peah-be Logistics API', 'Quarkus-based logistics API', 'api', false, 'lolmeida/peah-be', 'latest', 60, '/q/health/live', '/q/health/ready',
 '{"profile": "prod", "logging_level": "INFO"}',
 '["postgresql"]',
 '{"http": 8080}',
 '{"limits": {"memory": "512Mi", "cpu": "500m"}, "requests": {"memory": "256Mi", "cpu": "250m"}}');

-- Get app IDs
SET @postgresql_id = (SELECT id FROM config_apps WHERE name = 'postgresql');
SET @redis_id = (SELECT id FROM config_apps WHERE name = 'redis');
SET @prometheus_id = (SELECT id FROM config_apps WHERE name = 'prometheus');
SET @grafana_id = (SELECT id FROM config_apps WHERE name = 'grafana');
SET @n8n_id = (SELECT id FROM config_apps WHERE name = 'n8n');
SET @peahdb_id = (SELECT id FROM config_apps WHERE name = 'peahdb');

-- Insert app manifests for PostgreSQL
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@postgresql_id, 'DEPLOYMENT', true, 1, 'PostgreSQL deployment with persistent storage'),
(@postgresql_id, 'SERVICE', true, 2, 'PostgreSQL service for internal access'),
(@postgresql_id, 'PERSISTENT_VOLUME_CLAIM', true, 3, 'Persistent storage for PostgreSQL data'),
(@postgresql_id, 'SECRET', true, 4, 'PostgreSQL passwords and credentials');

-- Insert app manifests for Redis
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@redis_id, 'DEPLOYMENT', true, 1, 'Redis deployment'),
(@redis_id, 'SERVICE', true, 2, 'Redis service for internal access'),
(@redis_id, 'SECRET', true, 3, 'Redis authentication password');

-- Insert app manifests for Prometheus
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@prometheus_id, 'SERVICE_ACCOUNT', true, 1, 'Service account for Prometheus'),
(@prometheus_id, 'CLUSTER_ROLE', true, 2, 'Cluster role for metrics scraping'),
(@prometheus_id, 'CONFIG_MAP', true, 5, 'Prometheus configuration'),
(@prometheus_id, 'DEPLOYMENT', true, 10, 'Prometheus deployment'),
(@prometheus_id, 'SERVICE', true, 11, 'Prometheus service');

-- Insert app manifests for Grafana
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@grafana_id, 'DEPLOYMENT', true, 30, 'Grafana deployment'),
(@grafana_id, 'SERVICE', true, 31, 'Grafana service'),
(@grafana_id, 'SECRET', true, 32, 'Grafana admin credentials'),
(@grafana_id, 'INGRESS', true, 80, 'Grafana external access');

-- Insert app manifests for N8N
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@n8n_id, 'DEPLOYMENT', true, 50, 'N8N automation platform'),
(@n8n_id, 'SERVICE', true, 51, 'N8N service'),
(@n8n_id, 'SECRET', true, 52, 'N8N authentication credentials'),
(@n8n_id, 'PERSISTENT_VOLUME_CLAIM', true, 53, 'N8N workflow storage'),
(@n8n_id, 'INGRESS', true, 80, 'N8N external access');

-- Insert app manifests for PeahDB
INSERT INTO config_app_manifests (app_id, manifest_type, required, creation_priority, description) VALUES
(@peahdb_id, 'DEPLOYMENT', true, 60, 'peah-be API deployment'),
(@peahdb_id, 'SERVICE', true, 61, 'peah-be service'),
(@peahdb_id, 'INGRESS', true, 80, 'peah-be external access'),
(@peahdb_id, 'HPA', false, 90, 'Horizontal Pod Autoscaler');

-- Update the HPA manifest with condition
UPDATE config_app_manifests 
SET creation_condition = 'hpa.enabled' 
WHERE app_id = @peahdb_id AND manifest_type = 'HPA';

-- Also create stacks and apps for staging and dev environments (simplified version)
-- Staging
INSERT INTO config_stacks (environment_id, name, enabled, description) VALUES 
(@staging_env_id, 'database', true, 'Database services stack'),
(@staging_env_id, 'monitoring', true, 'Monitoring and observability stack'),
(@staging_env_id, 'apps', true, 'Applications stack');

-- Dev
INSERT INTO config_stacks (environment_id, name, enabled, description) VALUES 
(@dev_env_id, 'database', true, 'Database services stack'),
(@dev_env_id, 'monitoring', false, 'Monitoring and observability stack'),
(@dev_env_id, 'apps', true, 'Applications stack');