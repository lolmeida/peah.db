-- Clear existing data
DELETE FROM config_app_dependencies;
DELETE FROM config_app_manifests;
DELETE FROM config_apps;
DELETE FROM config_stacks;
DELETE FROM config_environments;

-- Reset sequences if you have them
-- ALTER SEQUENCE environments_id_seq RESTART WITH 1;
-- ALTER SEQUENCE stacks_id_seq RESTART WITH 1;
-- ALTER SEQUENCE apps_id_seq RESTART WITH 1;
-- ALTER SEQUENCE required_manifests_id_seq RESTART WITH 1;


-- Insert Environments
INSERT INTO config_environments (id, name, description, is_active) VALUES
(1, 'prod', 'Production environment', true),
(2, 'staging', 'Staging environment', true),
(3, 'dev', 'Development environment', true);

-- Insert Stacks
INSERT INTO config_stacks (id, name, enabled, description, environment_id) VALUES
-- Prod
(1, 'database', true, 'Database services stack', 1),
(2, 'monitoring', true, 'Monitoring and observability stack', 1),
(3, 'apps', false, 'Applications stack', 1),
-- Staging
(4, 'database', true, 'Database services stack', 2),
(5, 'monitoring', false, 'Monitoring and observability stack', 2),
(6, 'apps', true, 'Applications stack', 2),
-- Dev
(7, 'database', true, 'Database services stack', 3),
(8, 'monitoring', false, 'Monitoring and observability stack', 3),
(9, 'apps', true, 'Applications stack', 3);

-- Insert Apps, App Dependencies and Required Manifests

-- Stack: 1-database
-- App: postgresql
INSERT INTO config_apps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, requires_auth, description, stack_id) VALUES
(1, 'postgresql', 'PostgreSQL Database', true, 'database', 10, 'postgres', '14', '{"persistence":{"enabled":true,"size":"10Gi"},"auth":{"enabled":true,"type":"password","username":"postgres","database":"postgres","existingSecret":"postgresql-secret","secretKeys":{"adminPassword":"postgres-password","userPassword":"user-password"},"enableSuperuserAccess":true,"createUserDB":true,"allowEmptyPassword":false}}', '/health', true, 'PostgreSQL database with authentication', 1);
INSERT INTO config_app_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(1, 'DEPLOYMENT', true, 1, 'PostgreSQL deployment with persistent storage', 1),
(2, 'SERVICE', true, 2, 'PostgreSQL service for internal access', 1),
(3, 'PERSISTENT_VOLUME_CLAIM', true, 3, 'Persistent storage for PostgreSQL data', 1),
(4, 'SECRET', true, 4, 'PostgreSQL passwords and credentials', 1);

-- App: redis
INSERT INTO config_appsapps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, requires_auth, description, stack_id) VALUES
(2, 'redis', 'Redis Cache', true, 'database', 10, 'redis', '7-alpine', '{"auth":{"enabled":true,"type":"password","requirePass":true,"existingSecret":"redis-secret","secretKeys":{"password":"redis-password"},"aclFile":null,"enableACLUser":false,"usePasswordFiles":false}}', '/health', true, 'Redis cache with password authentication', 1);
INSERT INTO rconfig_required_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(5, 'DEPLOYMENT', true, 1, 'Redis deployment', 2),
(6, 'SERVICE', true, 2, 'Redis service for internal access', 2),
(7, 'SECRET', true, 3, 'Redis authentication password', 2);

-- Stack: 1-monitoring
-- App: prometheus
INSERT INTO config_apps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, requires_auth, description, stack_id) VALUES
(3, 'prometheus', 'Prometheus Monitoring', true, 'monitoring', 20, 'prom/prometheus', 'v2.45.0', '{"retention":"30d","scrapeInterval":"30s","auth":{"enabled":false,"type":"basic","basicAuth":{"username":"admin","existingSecret":"prometheus-auth-secret","secretKeys":{"username":"username","password":"password"}},"webConfig":null,"enableAdminAPI":false}}', '/healthy', false, 'Prometheus monitoring server', 2);
INSERT INTO config_required_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(8, 'SERVICE_ACCOUNT', true, 1, 'Service account for Prometheus', 3),
(9, 'CLUSTER_ROLE', true, 2, 'Cluster role for metrics scraping', 3),
(10, 'CONFIG_MAP', true, 5, 'Prometheus configuration', 3),
(11, 'DEPLOYMENT', true, 10, 'Prometheus deployment', 3),
(12, 'SERVICE', true, 11, 'Prometheus service', 3);

-- App: grafana
INSERT INTO config_apps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, requires_auth, description, stack_id) VALUES
(4, 'grafana', 'Grafana Dashboard', false, 'monitoring', 30, 'grafana/grafana', '10.2.0', '{"persistence":{"enabled":true,"size":"1Gi"},"auth":{"enabled":true,"type":"basic","adminUser":"admin","adminPassword":"admin","existingSecret":"grafana-secret","secretKeys":{"adminUser":"admin-user","adminPassword":"admin-password"},"disableLoginForm":false,"disableSignoutMenu":false,"autoAssignOrgRole":"Viewer","allowSignUp":false,"anonymousEnabled":false}}', '/api/health', true, 'Grafana dashboard with admin authentication', 2);
INSERT INTO config_app_dependencies (app_id, dependency_name) VALUES (4, 'prometheus');
INSERT INTO config_required_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(13, 'DEPLOYMENT', true, 30, 'Grafana deployment', 4),
(14, 'SERVICE', true, 31, 'Grafana service', 4),
(15, 'SECRET', true, 32, 'Grafana admin credentials', 4),
(16, 'INGRESS', true, 80, 'Grafana external access', 4);

-- Stack: 1-apps
-- App: n8n
INSERT INTO config_apps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, readiness_check_path, requires_auth, description, stack_id) VALUES
(5, 'n8n', 'N8N Automation', false, 'automation', 50, 'n8nio/n8n', 'latest', '{"timezone":"Europe/Lisbon","encryption_key":"n8n-secret-key","auth":{"enabled":true,"type":"email","defaultUser":{"email":"admin@n8n.local","firstName":"Admin","lastName":"User","password":"n8n-admin-password"},"existingSecret":"n8n-secret","secretKeys":{"encryptionKey":"encryption-key","adminPassword":"admin-password"},"jwtSecret":"jwt-secret-key","sessionSecret":"session-secret-key","disableUI":false,"enablePublicAPI":true}}', '/healthz', '/healthz', true, 'N8N automation platform with email authentication', 3);
INSERT INTO config_app_dependencies (app_id, dependency_name) VALUES (5, 'postgresql'), (5, 'redis');
INSERT INTO config_required_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(17, 'DEPLOYMENT', true, 50, 'N8N automation platform', 5),
(18, 'SERVICE', true, 51, 'N8N service', 5),
(19, 'SECRET', true, 52, 'N8N authentication credentials', 5),
(20, 'PERSISTENT_VOLUME_CLAIM', true, 53, 'N8N workflow storage', 5),
(21, 'INGRESS', true, 80, 'N8N external access', 5);

-- App: peahdb
INSERT INTO config_apps (id, name, display_name, enabled, category, deployment_priority, default_image_repository, default_image_tag, default_config, health_check_path, readiness_check_path, requires_auth, description, stack_id) VALUES
(6, 'peahdb', 'peah-be Logistics API', false, 'api', 60, 'lolmeida/peah-be', 'latest', '{"profile":"prod","logging_level":"INFO","auth":{"enabled":true,"type":"jwt","jwt":{"secret":"api-jwt-secret","issuer":"peahdb-api","expirationTime":"24h"},"existingSecret":"peahdb-auth-secret","secretKeys":{"jwtSecret":"jwt-secret","apiKey":"api-key"},"cors":{"enabled":true,"allowedOrigins":["*"],"allowCredentials":true},"rateLimit":{"enabled":true,"requestsPerMinute":100}}}', '/q/health/live', '/q/health/ready', true, 'peah-be API with JWT authentication', 3);
INSERT INTO config_app_dependencies (app_id, dependency_name) VALUES (6, 'postgresql');
INSERT INTO config_required_manifests (id, manifest_type, required, creation_priority, description, app_id) VALUES
(22, 'DEPLOYMENT', true, 60, 'peah-be API deployment', 6),
(23, 'SERVICE', true, 61, 'peah-be service', 6),
(24, 'INGRESS', true, 80, 'peah-be external access', 6),
(25, 'HPA', false, 90, 'Horizontal Pod Autoscaler', 6);
