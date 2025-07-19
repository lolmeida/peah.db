-- V2.2.0__Create_manifest_defaults_schema.sql
-- Manifest defaults configuration schema - Move hardcoded defaults to database

-- =================================================
-- MANIFEST DEFAULTS TABLES
-- =================================================

-- Categories for service types (database, monitoring, automation, api, etc.)
CREATE TABLE IF NOT EXISTS config_service_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    color VARCHAR(20) DEFAULT 'default',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default manifest configurations for each service category
CREATE TABLE IF NOT EXISTS config_manifest_defaults (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    manifest_type VARCHAR(50) NOT NULL,
    required BOOLEAN DEFAULT true,
    creation_priority INTEGER DEFAULT 100,
    description TEXT NOT NULL,
    creation_condition VARCHAR(200),
    default_config JSON,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_manifest (category_id, manifest_type),
    CONSTRAINT fk_manifest_default_category FOREIGN KEY (category_id) REFERENCES config_service_categories(id) ON DELETE CASCADE
);

-- Auth configurations templates for each service category and auth type
CREATE TABLE IF NOT EXISTS config_auth_defaults (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    default_config JSON NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_auth_type (category_id, auth_type),
    CONSTRAINT fk_auth_default_category FOREIGN KEY (category_id) REFERENCES config_service_categories(id) ON DELETE CASCADE
);

-- =================================================
-- INDEXES FOR PERFORMANCE
-- =================================================

CREATE INDEX idx_config_manifest_defaults_category ON config_manifest_defaults(category_id);
CREATE INDEX idx_config_manifest_defaults_type ON config_manifest_defaults(manifest_type);
CREATE INDEX idx_config_manifest_defaults_priority ON config_manifest_defaults(creation_priority);
CREATE INDEX idx_config_manifest_defaults_active ON config_manifest_defaults(is_active);

CREATE INDEX idx_config_auth_defaults_category ON config_auth_defaults(category_id);
CREATE INDEX idx_config_auth_defaults_type ON config_auth_defaults(auth_type);
CREATE INDEX idx_config_auth_defaults_active ON config_auth_defaults(is_active);

-- =================================================
-- SEED INITIAL DATA
-- =================================================

-- Insert service categories
INSERT INTO config_service_categories (name, display_name, description, icon, color) VALUES 
('database', 'Database Services', 'Database systems like PostgreSQL, Redis, MongoDB', '🗄️', 'primary'),
('monitoring', 'Monitoring Services', 'Monitoring and observability tools like Prometheus, Grafana', '📊', 'info'),
('automation', 'Automation Services', 'Workflow automation platforms like N8N, Airflow', '🤖', 'warning'),
('api', 'API Services', 'REST APIs, GraphQL services, microservices', '🚀', 'success'),
('messagequeue', 'Message Queue Services', 'Message brokers like RabbitMQ, Apache Kafka', '📡', 'secondary'),
('default', 'Default Category', 'Fallback category for unknown service types', '🔧', 'default');

-- Get category IDs for reference
SET @database_cat_id = (SELECT id FROM config_service_categories WHERE name = 'database');
SET @monitoring_cat_id = (SELECT id FROM config_service_categories WHERE name = 'monitoring');
SET @automation_cat_id = (SELECT id FROM config_service_categories WHERE name = 'automation');
SET @api_cat_id = (SELECT id FROM config_service_categories WHERE name = 'api');
SET @messagequeue_cat_id = (SELECT id FROM config_service_categories WHERE name = 'messagequeue');
SET @default_cat_id = (SELECT id FROM config_service_categories WHERE name = 'default');

-- =================================================
-- DATABASE CATEGORY DEFAULTS
-- =================================================

INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, creation_condition, default_config) VALUES
(@database_cat_id, 'DEPLOYMENT', true, 10, 'Database deployment with persistence and health checks', null,
 '{"replicaCount": 1, "restartPolicy": "Always", "imagePullPolicy": "IfNotPresent"}'),

(@database_cat_id, 'SERVICE', true, 20, 'Internal service for database access', null,
 '{"type": "ClusterIP", "sessionAffinity": "None"}'),

(@database_cat_id, 'PERSISTENT_VOLUME_CLAIM', true, 5, 'Persistent storage for database data', 'persistence.enabled',
 '{"accessMode": "ReadWriteOnce", "size": "10Gi", "storageClass": "default"}'),

(@database_cat_id, 'SECRET', true, 1, 'Database credentials and configuration secrets', 'auth.enabled',
 '{"type": "Opaque", "immutable": false}'),

(@database_cat_id, 'CONFIG_MAP', false, 15, 'Database configuration files', null,
 '{"immutable": false}');

-- =================================================
-- MONITORING CATEGORY DEFAULTS
-- =================================================

INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, creation_condition, default_config) VALUES
(@monitoring_cat_id, 'SERVICE_ACCOUNT', true, 1, 'Service account for monitoring permissions', null,
 '{"automountServiceAccountToken": true}'),

(@monitoring_cat_id, 'CLUSTER_ROLE', true, 2, 'Cluster role for monitoring access across namespaces', null,
 '{"createBinding": true, "rules": [{"apiGroups": [""], "resources": ["nodes", "services", "endpoints", "pods"], "verbs": ["get", "list", "watch"]}, {"apiGroups": ["extensions"], "resources": ["ingresses"], "verbs": ["get", "list", "watch"]}]}'),

(@monitoring_cat_id, 'DEPLOYMENT', true, 10, 'Monitoring service deployment', null,
 '{"replicaCount": 1, "restartPolicy": "Always"}'),

(@monitoring_cat_id, 'SERVICE', true, 20, 'Service for monitoring access', null,
 '{"type": "ClusterIP"}'),

(@monitoring_cat_id, 'INGRESS', false, 30, 'External access to monitoring dashboard', 'ingress.enabled',
 '{"className": "nginx", "tls": {"enabled": true}, "annotations": {"nginx.ingress.kubernetes.io/ssl-redirect": "true", "cert-manager.io/cluster-issuer": "letsencrypt-prod"}}'),

(@monitoring_cat_id, 'PERSISTENT_VOLUME_CLAIM', false, 5, 'Storage for monitoring data retention', 'persistence.enabled',
 '{"accessMode": "ReadWriteOnce", "size": "20Gi"}'),

(@monitoring_cat_id, 'SECRET', false, 3, 'Authentication credentials for monitoring access', 'auth.enabled',
 '{"type": "Opaque"}'),

(@monitoring_cat_id, 'CONFIG_MAP', true, 8, 'Monitoring configuration files', null,
 '{"immutable": false}');

-- =================================================
-- AUTOMATION CATEGORY DEFAULTS
-- =================================================

INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, creation_condition, default_config) VALUES
(@automation_cat_id, 'DEPLOYMENT', true, 10, 'Automation platform deployment', null,
 '{"replicaCount": 1, "restartPolicy": "Always"}'),

(@automation_cat_id, 'SERVICE', true, 20, 'Internal service for automation platform', null,
 '{"type": "ClusterIP"}'),

(@automation_cat_id, 'INGRESS', true, 30, 'External access to automation interface', null,
 '{"className": "nginx", "tls": {"enabled": true}, "annotations": {"nginx.ingress.kubernetes.io/ssl-redirect": "true", "cert-manager.io/cluster-issuer": "letsencrypt-prod"}}'),

(@automation_cat_id, 'PERSISTENT_VOLUME_CLAIM', false, 5, 'Storage for automation workflows and data', 'persistence.enabled',
 '{"accessMode": "ReadWriteOnce", "size": "5Gi"}'),

(@automation_cat_id, 'SECRET', true, 1, 'Authentication and encryption keys', null,
 '{"type": "Opaque", "immutable": false}'),

(@automation_cat_id, 'CONFIG_MAP', false, 15, 'Automation platform configuration', null,
 '{"immutable": false}');

-- =================================================
-- API CATEGORY DEFAULTS
-- =================================================

INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, creation_condition, default_config) VALUES
(@api_cat_id, 'DEPLOYMENT', true, 10, 'API service deployment with health checks', null,
 '{"replicaCount": 2, "restartPolicy": "Always", "strategy": {"type": "RollingUpdate", "rollingUpdate": {"maxSurge": 1, "maxUnavailable": 0}}}'),

(@api_cat_id, 'SERVICE', true, 20, 'Internal service for API access', null,
 '{"type": "ClusterIP", "sessionAffinity": "None"}'),

(@api_cat_id, 'INGRESS', true, 30, 'External API access with rate limiting', null,
 '{"className": "nginx", "tls": {"enabled": true}, "annotations": {"nginx.ingress.kubernetes.io/ssl-redirect": "true", "cert-manager.io/cluster-issuer": "letsencrypt-prod", "nginx.ingress.kubernetes.io/rate-limit": "100", "nginx.ingress.kubernetes.io/rate-limit-window": "1m"}}'),

(@api_cat_id, 'HPA', false, 40, 'Horizontal Pod Autoscaler for API scaling', 'hpa.enabled',
 '{"minReplicas": 2, "maxReplicas": 10, "targetCPUUtilizationPercentage": 70, "targetMemoryUtilizationPercentage": 80}'),

(@api_cat_id, 'SECRET', true, 1, 'API keys, JWT secrets, and database credentials', null,
 '{"type": "Opaque", "immutable": false}'),

(@api_cat_id, 'CONFIG_MAP', true, 8, 'API configuration and environment variables', null,
 '{"immutable": false}'),

(@api_cat_id, 'SERVICE_ACCOUNT', false, 2, 'Service account for API permissions', 'serviceAccount.create',
 '{"automountServiceAccountToken": true}');

-- =================================================
-- DEFAULT CATEGORY DEFAULTS
-- =================================================

INSERT INTO config_manifest_defaults (category_id, manifest_type, required, creation_priority, description, creation_condition, default_config) VALUES
(@default_cat_id, 'DEPLOYMENT', true, 10, 'Application deployment', null,
 '{"replicaCount": 1, "restartPolicy": "Always"}'),

(@default_cat_id, 'SERVICE', true, 20, 'Internal service access', null,
 '{"type": "ClusterIP"}');

-- =================================================
-- AUTH DEFAULTS BY CATEGORY
-- =================================================

-- Database auth defaults
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config) VALUES
(@database_cat_id, 'password', 'Password Authentication', 'Username/password based authentication for databases',
 '{"enabled": true, "type": "password", "username": "admin", "database": "main", "existingSecret": "database-secret", "secretKeys": {"adminPassword": "admin-password", "userPassword": "user-password"}, "enableSuperuserAccess": true, "createUserDB": true, "allowEmptyPassword": false}'),

(@database_cat_id, 'certificate', 'Certificate Authentication', 'SSL certificate based authentication',
 '{"enabled": true, "type": "certificate", "certFile": "/etc/certs/tls.crt", "keyFile": "/etc/certs/tls.key", "caFile": "/etc/certs/ca.crt", "existingSecret": "database-cert-secret"}');

-- Monitoring auth defaults
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config) VALUES
(@monitoring_cat_id, 'basic', 'Basic Authentication', 'HTTP Basic authentication for monitoring access',
 '{"enabled": true, "type": "basic", "adminUser": "admin", "existingSecret": "monitoring-secret", "secretKeys": {"username": "username", "password": "password"}, "autoAssignOrgRole": "Viewer", "allowSignUp": false, "anonymousEnabled": false}'),

(@monitoring_cat_id, 'oauth2', 'OAuth2 Authentication', 'OAuth2 based authentication',
 '{"enabled": true, "type": "oauth2", "clientId": "monitoring-client", "clientSecret": "monitoring-secret", "authorizeUrl": "https://auth.provider.com/oauth2/authorize", "tokenUrl": "https://auth.provider.com/oauth2/token", "scope": "read"}');

-- Automation auth defaults
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config) VALUES
(@automation_cat_id, 'email', 'Email Authentication', 'Email-based user authentication',
 '{"enabled": true, "type": "email", "defaultUser": {"email": "admin@automation.local", "firstName": "Admin", "lastName": "User"}, "existingSecret": "automation-secret", "secretKeys": {"encryptionKey": "encryption-key", "adminPassword": "admin-password"}, "jwtSecret": "jwt-secret-key", "sessionSecret": "session-secret-key", "disableUI": false, "enablePublicAPI": true}'),

(@automation_cat_id, 'ldap', 'LDAP Authentication', 'LDAP directory authentication',
 '{"enabled": true, "type": "ldap", "serverUrl": "ldap://ldap.company.com:389", "bindDn": "cn=admin,dc=company,dc=com", "baseDn": "dc=company,dc=com", "userSearchFilter": "(uid={{username}})", "groupSearchFilter": "(memberUid={{username}})"}');

-- API auth defaults
INSERT INTO config_auth_defaults (category_id, auth_type, display_name, description, default_config) VALUES
(@api_cat_id, 'jwt', 'JWT Authentication', 'JSON Web Token authentication',
 '{"enabled": true, "type": "jwt", "jwt": {"secret": "api-jwt-secret", "issuer": "api-service", "expirationTime": "24h"}, "existingSecret": "api-auth-secret", "secretKeys": {"jwtSecret": "jwt-secret", "apiKey": "api-key"}, "cors": {"enabled": true, "allowedOrigins": ["*"], "allowCredentials": true}, "rateLimit": {"enabled": true, "requestsPerMinute": 100}}'),

(@api_cat_id, 'oauth2', 'OAuth2 Authentication', 'OAuth2 token-based authentication',
 '{"enabled": true, "type": "oauth2", "clientId": "api-client-id", "clientSecret": "api-client-secret", "authorizeUrl": "https://auth.provider.com/oauth2/authorize", "tokenUrl": "https://auth.provider.com/oauth2/token", "scope": "read write", "introspectionUrl": "https://auth.provider.com/oauth2/introspect"}'),

(@api_cat_id, 'basic', 'Basic Authentication', 'HTTP Basic authentication for API access',
 '{"enabled": true, "type": "basic", "username": "api-user", "existingSecret": "api-basic-secret", "secretKeys": {"username": "username", "password": "password"}, "realm": "API Access"}'),

(@api_cat_id, 'api-key', 'API Key Authentication', 'API key based authentication',
 '{"enabled": true, "type": "api-key", "headerName": "X-API-Key", "queryParam": "api_key", "existingSecret": "api-key-secret", "secretKeys": {"apiKey": "api-key"}, "allowMultipleKeys": true}'); 