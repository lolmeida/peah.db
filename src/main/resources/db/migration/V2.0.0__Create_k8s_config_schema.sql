-- V2.0.0__Create_k8s_config_schema.sql
-- K8s configuration management schema

-- =================================================
-- CORE STRUCTURE TABLES
-- =================================================

-- Environments (prod, staging, dev)
CREATE TABLE IF NOT EXISTS config_environments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Stacks per environment
CREATE TABLE IF NOT EXISTS config_stacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    environment_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT false,
    description TEXT,
    config JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stack_env_name (environment_id, name),
    CONSTRAINT fk_stack_environment FOREIGN KEY (environment_id) REFERENCES config_environments(id)
);

-- Apps within each stack
CREATE TABLE IF NOT EXISTS config_apps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stack_id BIGINT NOT NULL,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    description TEXT,
    category VARCHAR(50),
    enabled BOOLEAN DEFAULT false,
    version VARCHAR(50),
    default_image_repository VARCHAR(200),
    default_image_tag VARCHAR(50) DEFAULT 'latest',
    default_config JSON,
    dependencies JSON,
    default_ports JSON,
    default_resources JSON,
    deployment_priority INTEGER DEFAULT 100,
    health_check_path VARCHAR(100),
    readiness_check_path VARCHAR(100),
    documentation_url VARCHAR(200),
    icon_url VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_stack FOREIGN KEY (stack_id) REFERENCES config_stacks(id)
);

-- App to Manifest mappings
CREATE TABLE IF NOT EXISTS config_app_manifests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    manifest_type VARCHAR(50) NOT NULL,
    required BOOLEAN DEFAULT true,
    creation_priority INTEGER DEFAULT 100,
    creation_condition VARCHAR(100),
    default_config JSON,
    template_overrides JSON,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_manifest (app_id, manifest_type),
    CONSTRAINT fk_manifest_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- =================================================
-- KUBERNETES RESOURCES TABLES
-- =================================================

-- K8s Deployments
CREATE TABLE IF NOT EXISTS config_deployments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    replica_count INTEGER DEFAULT 1,
    image_repository VARCHAR(200),
    image_tag VARCHAR(50) DEFAULT 'latest',
    container_port INTEGER,
    env_vars JSON,
    resources JSON,
    liveness_probe JSON,
    readiness_probe JSON,
    volumes JSON,
    node_selector JSON,
    tolerations JSON,
    affinity JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_deployment_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s Services
CREATE TABLE IF NOT EXISTS config_kubernetes_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    service_type VARCHAR(20) DEFAULT 'ClusterIP',
    ports JSON,
    selector JSON,
    session_affinity VARCHAR(20) DEFAULT 'None',
    cluster_ip VARCHAR(50),
    external_name VARCHAR(100),
    load_balancer_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s Ingresses
CREATE TABLE IF NOT EXISTS config_ingresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    ingress_class_name VARCHAR(50),
    rules JSON,
    tls JSON,
    annotations JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ingress_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s PersistentVolumeClaims
CREATE TABLE IF NOT EXISTS config_persistent_volume_claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    access_mode VARCHAR(20) DEFAULT 'ReadWriteOnce',
    size VARCHAR(20) DEFAULT '5Gi',
    storage_class_name VARCHAR(50),
    volume_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pvc_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s ConfigMaps
CREATE TABLE IF NOT EXISTS config_configmaps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    data JSON,
    binary_data JSON,
    immutable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_configmap_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s Secrets
CREATE TABLE IF NOT EXISTS config_secrets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    secret_type VARCHAR(50) DEFAULT 'Opaque',
    data JSON,
    string_data JSON,
    immutable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_secret_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s ServiceAccounts
CREATE TABLE IF NOT EXISTS config_service_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    image_pull_secrets JSON,
    secrets JSON,
    automount_service_account_token BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_serviceaccount_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s ClusterRoles
CREATE TABLE IF NOT EXISTS config_cluster_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    rules JSON,
    aggregation_rule JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_clusterrole_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- K8s HorizontalPodAutoscalers
CREATE TABLE IF NOT EXISTS config_hpa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSON,
    min_replicas INTEGER DEFAULT 1,
    max_replicas INTEGER DEFAULT 5,
    target_cpu_utilization_percentage INTEGER DEFAULT 70,
    target_memory_utilization_percentage INTEGER,
    metrics JSON,
    behavior JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hpa_app FOREIGN KEY (app_id) REFERENCES config_apps(id) ON DELETE CASCADE
);

-- =================================================
-- INDEXES FOR PERFORMANCE
-- =================================================

CREATE INDEX idx_config_stacks_environment ON config_stacks(environment_id);
CREATE INDEX idx_config_apps_stack ON config_apps(stack_id);
CREATE INDEX idx_config_apps_category ON config_apps(category);
CREATE INDEX idx_config_apps_priority ON config_apps(deployment_priority);
CREATE INDEX idx_config_app_manifests_app ON config_app_manifests(app_id);
CREATE INDEX idx_config_app_manifests_type ON config_app_manifests(manifest_type);

-- Indexes for Kubernetes resources
CREATE INDEX idx_config_deployments_app ON config_deployments(app_id);
CREATE INDEX idx_config_services_app ON config_kubernetes_services(app_id);
CREATE INDEX idx_config_ingresses_app ON config_ingresses(app_id);
CREATE INDEX idx_config_pvcs_app ON config_persistent_volume_claims(app_id);
CREATE INDEX idx_config_configmaps_app ON config_configmaps(app_id);
CREATE INDEX idx_config_secrets_app ON config_secrets(app_id);
CREATE INDEX idx_config_serviceaccounts_app ON config_service_accounts(app_id);
CREATE INDEX idx_config_clusterroles_app ON config_cluster_roles(app_id);
CREATE INDEX idx_config_hpa_app ON config_hpa(app_id);