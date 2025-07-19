# 🚀 **IMPLEMENTATION GUIDE - Database-Driven Config**

**Documento Único para Implementação Completa**  
**Data**: $(date +"%Y-%m-%d")  
**Status**: 📋 **READY TO IMPLEMENT**

---

## 📋 **SUMÁRIO EXECUTIVO**

### **🎯 O que vamos construir:**
Sistema **database-driven** para gestão de configurações Kubernetes via interface web, substituindo arquivos YAML estáticos por configuração dinâmica.

### **🏗️ Arquitetura:**
```
Frontend (React) → API (Quarkus) → Database (PostgreSQL)
                        ↓
                 JSON Values → Helm Deploy
```

### **📊 Stacks:**
- **database**: Redis, PostgreSQL, MySQL
- **monitoring**: Grafana, Prometheus
- **apps**: N8N, Peah-DB, Logistics-API

---

## 📊 **ESTRUTURA DE DADOS COMPLETA**

### **🏗️ Hierarquia Database-Driven:**

```
Environment (prod, staging, dev)
├── Stack (database, monitoring, apps)  
│   ├── App (postgresql, n8n, grafana, etc.)
│   │   ├── AppManifest (DEPLOYMENT, SERVICE, etc.)
│   │   │   └── ManifestType (ENUM)
│   │   └── Kubernetes Resources:
│   │       ├── Deployment
│   │       ├── KubernetesService
│   │       ├── Ingress
│   │       ├── PersistentVolumeClaim
│   │       ├── ConfigMap
│   │       ├── Secret
│   │       ├── ServiceAccount
│   │       ├── ClusterRole
│   │       └── Hpa
```

### **📋 Total: 15 Java Entity Classes**

| **Entity Class** | **Table** | **Purpose** | **Relationships** |
|---|---|---|---|
| `Environment.java` | `config_environments` | prod, staging, dev | → Stack (1:N) |
| `Stack.java` | `config_stacks` | database, monitoring, apps | → App (1:N) |
| `App.java` | `config_apps` | postgresql, n8n, grafana | → AppManifest (1:N) |
| `AppManifest.java` | `config_app_manifests` | App↔Manifest mapping | ManifestType (enum) |
| **Kubernetes Resources:** |
| `Deployment.java` | `config_deployments` | K8s Deployments | → App (N:1) |
| `KubernetesService.java` | `config_kubernetes_services` | K8s Services | → App (N:1) |
| `Ingress.java` | `config_ingresses` | K8s Ingresses | → App (N:1) |
| `PersistentVolumeClaim.java` | `config_persistent_volume_claims` | K8s PVCs | → App (N:1) |
| `ConfigMap.java` | `config_configmaps` | K8s ConfigMaps | → App (N:1) |
| `Secret.java` | `config_secrets` | K8s Secrets | → App (N:1) |
| `ServiceAccount.java` | `config_service_accounts` | K8s ServiceAccounts | → App (N:1) |
| `ClusterRole.java` | `config_cluster_roles` | K8s ClusterRoles | → App (N:1) |
| `Hpa.java` | `config_hpa` | K8s HPAs | → App (N:1) |

### **🎯 Apps Definidas com Manifestos:**

| **App** | **Category** | **Priority** | **Manifestos** | **Dependencies** |
|---|---|---|---|---|
| **postgresql** | database | 10 | Deployment, Service, PVC, Secret | None |
| **redis** | database | 10 | Deployment, Service, Secret | None |
| **prometheus** | monitoring | 20 | SA, ClusterRole, ConfigMap, Deployment, Service, PVC | None |
| **grafana** | monitoring | 30 | Deployment, Service, Secret, Ingress | prometheus |
| **n8n** | automation | 50 | Deployment, Service, Secret, PVC, Ingress | postgresql, redis |
| **peahdb** | api | 60 | Deployment, Service, Ingress, HPA | postgresql |

---

## 🗄️ **FASE 1: DATABASE SETUP**

### **1.1. PostgreSQL Schema**

```sql
-- schema.sql - Complete Database Schema for Apps & Manifests

-- ======================================
-- CORE STRUCTURE TABLES
-- ======================================

-- Environments (prod, staging, dev)
CREATE TABLE config_environments (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Stacks per environment
CREATE TABLE config_stacks (
    id SERIAL PRIMARY KEY,
    environment_id INTEGER REFERENCES config_environments(id),
    name VARCHAR(50) NOT NULL,              -- 'database', 'monitoring', 'apps'
    enabled BOOLEAN DEFAULT false,
    description TEXT,
    config JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(environment_id, name)
);

-- Apps within each stack
CREATE TABLE config_apps (
    id SERIAL PRIMARY KEY,
    stack_id INTEGER REFERENCES config_stacks(id),
    name VARCHAR(50) UNIQUE NOT NULL,       -- 'postgresql', 'n8n', 'grafana'
    display_name VARCHAR(100),
    description TEXT,
    category VARCHAR(50),                   -- 'database', 'monitoring', 'automation'
    enabled BOOLEAN DEFAULT false,
    default_image_repository VARCHAR(200),
    default_image_tag VARCHAR(50) DEFAULT 'latest',
    default_config JSONB DEFAULT '{}',
    dependencies JSONB DEFAULT '[]',        -- ["postgresql", "redis"]
    default_ports JSONB DEFAULT '{}',
    default_resources JSONB DEFAULT '{}',
    deployment_priority INTEGER DEFAULT 100,
    health_check_path VARCHAR(100),
    readiness_check_path VARCHAR(100),
    documentation_url VARCHAR(200),
    icon_url VARCHAR(200),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- App to Manifest mappings
CREATE TABLE config_app_manifests (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    manifest_type VARCHAR(50) NOT NULL,    -- 'DEPLOYMENT', 'SERVICE', 'INGRESS', etc.
    required BOOLEAN DEFAULT true,
    creation_priority INTEGER DEFAULT 100,
    creation_condition VARCHAR(100),       -- 'persistence.enabled', 'ingress.enabled'
    default_config JSONB DEFAULT '{}',
    template_overrides JSONB DEFAULT '{}',
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(app_id, manifest_type)
);

-- ======================================
-- KUBERNETES RESOURCES TABLES
-- ======================================

-- K8s Deployments
CREATE TABLE config_deployments (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    replica_count INTEGER DEFAULT 1,
    image_repository VARCHAR(200),
    image_tag VARCHAR(50) DEFAULT 'latest',
    container_port INTEGER,
    env_vars JSONB DEFAULT '{}',
    resources JSONB DEFAULT '{}',
    liveness_probe JSONB DEFAULT '{}',
    readiness_probe JSONB DEFAULT '{}',
    volumes JSONB DEFAULT '{}',
    node_selector JSONB DEFAULT '{}',
    tolerations JSONB DEFAULT '[]',
    affinity JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s Services
CREATE TABLE config_kubernetes_services (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    service_type VARCHAR(20) DEFAULT 'ClusterIP',
    ports JSONB DEFAULT '{}',
    selector JSONB DEFAULT '{}',
    session_affinity VARCHAR(20) DEFAULT 'None',
    cluster_ip VARCHAR(50),
    external_name VARCHAR(100),
    load_balancer_ip VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s Ingresses  
CREATE TABLE config_ingresses (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    ingress_class_name VARCHAR(50),
    rules JSONB DEFAULT '[]',
    tls JSONB DEFAULT '[]',
    annotations JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s PersistentVolumeClaims
CREATE TABLE config_persistent_volume_claims (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    access_mode VARCHAR(20) DEFAULT 'ReadWriteOnce',
    size VARCHAR(20) DEFAULT '5Gi',
    storage_class_name VARCHAR(50),
    volume_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s ConfigMaps
CREATE TABLE config_configmaps (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    data JSONB DEFAULT '{}',
    binary_data JSONB DEFAULT '{}',
    immutable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s Secrets
CREATE TABLE config_secrets (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    secret_type VARCHAR(50) DEFAULT 'Opaque',
    data JSONB DEFAULT '{}',
    string_data JSONB DEFAULT '{}',
    immutable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s ServiceAccounts
CREATE TABLE config_service_accounts (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    image_pull_secrets JSONB DEFAULT '[]',
    secrets JSONB DEFAULT '[]',
    automount_service_account_token BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s ClusterRoles
CREATE TABLE config_cluster_roles (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    rules JSONB DEFAULT '[]',
    aggregation_rule JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- K8s HorizontalPodAutoscalers
CREATE TABLE config_hpa (
    id SERIAL PRIMARY KEY,
    app_id INTEGER REFERENCES config_apps(id) ON DELETE CASCADE,
    enabled BOOLEAN DEFAULT false,
    metadata_name VARCHAR(100),
    metadata_labels JSONB DEFAULT '{}',
    min_replicas INTEGER DEFAULT 1,
    max_replicas INTEGER DEFAULT 5,
    target_cpu_utilization_percentage INTEGER DEFAULT 70,
    target_memory_utilization_percentage INTEGER,
    metrics JSONB DEFAULT '[]',
    behavior JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ======================================
-- AUDIT AND DEPLOYMENT TRACKING
-- ======================================

-- Audit log
CREATE TABLE config_audit (
    id SERIAL PRIMARY KEY,
    environment_id INTEGER REFERENCES config_environments(id),
    stack_name VARCHAR(50),
    app_name VARCHAR(50),
    change_type VARCHAR(20) CHECK (change_type IN ('CREATE', 'UPDATE', 'DELETE')),
    old_config JSONB,
    new_config JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT NOW()
);

-- Deployment tracking
CREATE TABLE config_deployment_logs (
    id SERIAL PRIMARY KEY,
    environment_id INTEGER REFERENCES config_environments(id),
    stack_name VARCHAR(50),
    app_name VARCHAR(50),
    status VARCHAR(20) CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    helm_values JSONB,
    deploy_log TEXT,
    deployed_by VARCHAR(100),
    deployed_at TIMESTAMP DEFAULT NOW()
);

-- ======================================
-- INDEXES FOR PERFORMANCE
-- ======================================

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
-- ... (similar indexes for other K8s resources)
```

### **1.2. Seed Data**

```sql
-- seed-data.sql
-- Criar ambientes
INSERT INTO config_environments (name, description) VALUES 
('prod', 'Production environment'),
('staging', 'Staging environment'),
('dev', 'Development environment');

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
```

### **1.3. Docker Setup**

```bash
# docker-compose.yml para desenvolvimento
version: '3.8'
services:
  config-db:
    image: postgres:14
    environment:
      POSTGRES_DB: config
      POSTGRES_USER: config
      POSTGRES_PASSWORD: config123
    ports:
      - "5432:5432"
    volumes:
      - ./schema.sql:/docker-entrypoint-initdb.d/1-schema.sql
      - ./seed-data.sql:/docker-entrypoint-initdb.d/2-seed-data.sql
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

**Comandos:**
```bash
# Iniciar database
docker-compose up -d config-db

# Verificar
docker exec -it config-api-config-db-1 psql -U config -d config -c "\dt"
```

---

## 🚀 **FASE 2: REST API (QUARKUS)**

### **2.1. Project Setup**

```bash
# Criar projeto Quarkus
mvn io.quarkus.platform:quarkus-maven-plugin:3.15.1:create \
    -DprojectGroupId=com.lolmeida \
    -DprojectArtifactId=config-api \
    -DclassName="com.lolmeida.config.ConfigResource" \
    -Dpath="/api/config" \
    -Dextensions="hibernate-orm-panache,jdbc-postgresql,resteasy-reactive-jackson,smallrye-openapi"

cd config-api
```

### **2.2. Configuration (application.yml)**

```yaml
# src/main/resources/application.yml
quarkus:
  application:
    name: config-api
  
  datasource:
    db-kind: postgresql
    username: config
    password: config123
    jdbc:
      url: jdbc:postgresql://localhost:5432/config
  
  hibernate-orm:
    database:
      generation: none
    log:
      sql: true
  
  http:
    port: 8080
    cors:
      ~: true
      origins: http://localhost:3000
      headers: "*"
      methods: "*"

  smallrye-openapi:
    info-title: Configuration API
    info-version: 1.0.0
```

### **2.3. Entity Classes**

#### **🏗️ Core Structure Entities**

```java
// src/main/java/com/lolmeida/config/entity/Environment.java
@Entity
@Table(name = "config_environments")
public class Environment extends PanacheEntity {
    public String name;
    public String description;
    @Column(name = "is_active")
    public Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}

// src/main/java/com/lolmeida/config/entity/Stack.java
@Entity
@Table(name = "config_stacks")
public class Stack extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "environment_id")
    public Environment environment;
    
    public String name;
    public Boolean enabled = false;
    public String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode config;
    
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "stack", cascade = CascadeType.ALL)
    public List<App> apps = new ArrayList<>();
}

// src/main/java/com/lolmeida/config/entity/App.java
@Entity
@Table(name = "config_apps")
public class App extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "stack_id")
    public Stack stack;
    
    public Boolean enabled = false;
    
    @Column(name = "name", unique = true)
    public String name; // postgresql, n8n, grafana, etc.
    
    @Column(name = "display_name")
    public String displayName;
    
    @Column(name = "description")
    public String description;
    
    @Column(name = "category")
    public String category; // database, monitoring, automation, api
    
    @Column(name = "default_image_repository")
    public String defaultImageRepository;
    
    @Column(name = "default_image_tag")
    public String defaultImageTag = "latest";
    
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode defaultConfig;
    
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode dependencies; // ["postgresql", "redis"]
    
    @Column(name = "deployment_priority")
    public Integer deploymentPriority = 100;
    
    @Column(name = "health_check_path")
    public String healthCheckPath;
    
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL)
    public List<AppManifest> requiredManifests = new ArrayList<>();
    
    // Helper methods
    public boolean isDatabaseApp() { return "database".equals(category); }
    public boolean isMonitoringApp() { return "monitoring".equals(category); }
}

// src/main/java/com/lolmeida/config/entity/AppManifest.java
@Entity
@Table(name = "config_app_manifests")
public class AppManifest extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "app_id")
    public App app;
    
    @Column(name = "manifest_type")
    @Enumerated(EnumType.STRING)
    public ManifestType manifestType; // DEPLOYMENT, SERVICE, INGRESS, etc.
    
    @Column(name = "required")
    public Boolean required = true;
    
    @Column(name = "creation_priority")
    public Integer creationPriority = 100;
    
    @Column(name = "creation_condition")
    public String creationCondition; // "persistence.enabled", "ingress.enabled"
    
    @JdbcTypeCode(SqlTypes.JSON)
    public JsonNode defaultConfig;
}

// ManifestType Enum
enum ManifestType {
    DEPLOYMENT("Deployment", "deployments.yaml", Deployment.class),
    SERVICE("Service", "services.yaml", KubernetesService.class),
    INGRESS("Ingress", "ingresses.yaml", Ingress.class),
    PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim", "persistentvolumeclaims.yaml", PersistentVolumeClaim.class),
    CONFIG_MAP("ConfigMap", "configmaps.yaml", ConfigMap.class),
    SECRET("Secret", "secret.yaml", Secret.class),
    SERVICE_ACCOUNT("ServiceAccount", "serviceaccounts.yaml", ServiceAccount.class),
    CLUSTER_ROLE("ClusterRole", "clusterroles.yaml", ClusterRole.class),
    HPA("HorizontalPodAutoscaler", "hpa.yaml", Hpa.class);
    
    private final String kubernetesKind;
    private final String templateFile;
    private final Class<?> entityClass;
    
    // Getters and helper methods...
}
```

#### **🎯 Kubernetes Manifest Entities (9 classes)**

```java
// src/main/java/com/lolmeida/config/entity/Deployment.java
@Entity
@Table(name = "config_deployments")
public class Deployment extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "app_id")
    public App app;
    
    public Boolean enabled = false;
    
    // Metadata
    @Column(name = "metadata_name")
    public String metadataName;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_labels")
    public JsonNode metadataLabels;
    
    // Spec fields
    @Column(name = "replica_count")
    public Integer replicaCount = 1;
    
    @Column(name = "image_repository")
    public String imageRepository;
    
    @Column(name = "image_tag")
    public String imageTag = "latest";
    
    @Column(name = "container_port")
    public Integer containerPort;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "env_vars")
    public JsonNode envVars;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resources")
    public JsonNode resources;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "liveness_probe")
    public JsonNode livenessProbe;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "readiness_probe")
    public JsonNode readinessProbe;
    
    // Helper methods
    public String getFullImageName() { return imageRepository + ":" + imageTag; }
}

// Similar structure for:
// - KubernetesService.java (Service)
// - Ingress.java  
// - PersistentVolumeClaim.java
// - ConfigMap.java
// - Secret.java
// - ServiceAccount.java
// - ClusterRole.java
// - Hpa.java
```

### **2.4. REST Endpoints**

```java
// src/main/java/com/lolmeida/config/resource/ConfigResource.java
@Path("/api/config")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConfigResource {

    // ENVIRONMENTS
    @GET
    @Path("/environments")
    public List<Environment> getEnvironments() {
        return Environment.listAll();
    }

    @POST
    @Path("/environments")
    @Transactional
    public Environment createEnvironment(Environment environment) {
        environment.persist();
        return environment;
    }

    // STACKS
    @GET
    @Path("/environments/{envId}/stacks")
    public List<Stack> getStacks(@PathParam("envId") Long envId) {
        return Stack.find("environment.id", envId).list();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackName}")
    @Transactional
    public Stack updateStack(@PathParam("envId") Long envId, 
                           @PathParam("stackName") String stackName,
                           Stack updatedStack) {
        Stack stack = Stack.find("environment.id = ?1 and name = ?2", envId, stackName).firstResult();
        if (stack != null) {
            stack.enabled = updatedStack.enabled;
            stack.config = updatedStack.config;
        }
        return stack;
    }

    // APPS
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/apps")
    public List<App> getApps(@PathParam("envId") Long envId,
                           @PathParam("stackName") String stackName) {
        return App.find(
            "stack.environment.id = ?1 and stack.name = ?2 ORDER BY deploymentPriority, name", 
            envId, stackName
        ).list();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}")
    @Transactional
    public App updateApp(@PathParam("envId") Long envId,
                       @PathParam("stackName") String stackName,
                       @PathParam("appName") String appName,
                       App updatedApp) {
        App app = App.find(
            "stack.environment.id = ?1 and stack.name = ?2 and name = ?3",
            envId, stackName, appName
        ).firstResult();
        
        if (app != null) {
            app.enabled = updatedApp.enabled;
            app.defaultConfig = updatedApp.defaultConfig;
            app.deploymentPriority = updatedApp.deploymentPriority;
            // Update other fields as needed
        }
        return app;
    }

    // APP MANIFESTS
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}/manifests")
    public List<AppManifest> getAppManifests(@PathParam("envId") Long envId,
                                           @PathParam("stackName") String stackName,
                                           @PathParam("appName") String appName) {
        return AppManifest.find(
            "app.stack.environment.id = ?1 and app.stack.name = ?2 and app.name = ?3 ORDER BY creationPriority",
            envId, stackName, appName
        ).list();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}/manifests/{manifestType}")
    @Transactional
    public AppManifest updateAppManifest(@PathParam("envId") Long envId,
                                       @PathParam("stackName") String stackName,
                                       @PathParam("appName") String appName,
                                       @PathParam("manifestType") String manifestType,
                                       AppManifest updatedManifest) {
        AppManifest manifest = AppManifest.find(
            "app.stack.environment.id = ?1 and app.stack.name = ?2 and app.name = ?3 and manifestType = ?4",
            envId, stackName, appName, ManifestType.valueOf(manifestType.toUpperCase())
        ).firstResult();
        
        if (manifest != null) {
            manifest.required = updatedManifest.required;
            manifest.defaultConfig = updatedManifest.defaultConfig;
            manifest.creationCondition = updatedManifest.creationCondition;
        }
        return manifest;
    }

    // KUBERNETES RESOURCES (Direct Access)
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}/deployments")
    public List<Deployment> getDeployments(@PathParam("envId") Long envId,
                                         @PathParam("stackName") String stackName,
                                         @PathParam("appName") String appName) {
        return Deployment.find(
            "app.stack.environment.id = ?1 and app.stack.name = ?2 and app.name = ?3",
            envId, stackName, appName
        ).list();
    }
    
    // Similar endpoints for other K8s resources...

    // VALUES GENERATION
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/values")
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode generateValues(@PathParam("envId") Long envId,
                                 @PathParam("stackName") String stackName) {
        return valuesGenerator.generateStackValues(envId, stackName);
    }

    @GET
    @Path("/environments/{envId}/stacks/{stackName}/values.yaml")
    @Produces("application/x-yaml")
    public String generateValuesYaml(@PathParam("envId") Long envId,
                                   @PathParam("stackName") String stackName) {
        JsonNode values = valuesGenerator.generateStackValues(envId, stackName);
        return yamlMapper.writeValueAsString(values);
    }
}
```

### **2.5. Values Generator Service**

```java
// src/main/java/com/lolmeida/config/service/ValuesGeneratorService.java
@ApplicationScoped
public class ValuesGeneratorService {
    
    @Inject
    ObjectMapper objectMapper;

    public JsonNode generateStackValues(Long envId, String stackName) {
        ObjectNode values = objectMapper.createObjectNode();
        
        // Global configuration
        Environment env = Environment.findById(envId);
        ObjectNode global = values.putObject("global");
        global.put("namespace", env.name.equals("prod") ? "lolmeida" : env.name);
        global.put("timezone", "Europe/Lisbon");

        // Stack configuration
        Stack stack = Stack.find("environment.id = ?1 and name = ?2", envId, stackName).firstResult();
        if (stack == null) return values;

        // Stack-level flags
        ObjectNode stackConfig = values.putObject(stackName + "Stack");
        stackConfig.put("enabled", stack.enabled);
        
        ObjectNode stackApps = stackConfig.putObject("apps");
        
        // Apps configuration (sorted by deployment priority)
        List<App> apps = App.find("stack.id = ?1 ORDER BY deploymentPriority, name", stack.id).list();
        for (App app : apps) {
            stackApps.put(app.name, app.enabled);
            
            if (app.enabled) {
                ObjectNode appConfig = values.putObject(app.name);
                appConfig.put("enabled", true);
                
                // App-specific configuration
                if (app.defaultConfig != null) {
                    appConfig.setAll((ObjectNode) app.defaultConfig);
                }
                
                // Image configuration
                ObjectNode image = appConfig.putObject("image");
                image.put("repository", app.defaultImageRepository);
                image.put("tag", app.defaultImageTag);
                
                // Default ports
                if (app.defaultPorts != null) {
                    appConfig.set("ports", app.defaultPorts);
                }
                
                // Default resources
                if (app.defaultResources != null) {
                    appConfig.set("resources", app.defaultResources);
                }
                
                // Health checks
                if (app.healthCheckPath != null) {
                    appConfig.put("healthCheckPath", app.healthCheckPath);
                }
                
                // Generate manifest-specific configurations
                generateAppManifestConfigurations(app, appConfig);
            }
        }

        return values;
    }
    
    private void generateAppManifestConfigurations(App app, ObjectNode appConfig) {
        // Get all required manifests for this app
        List<AppManifest> manifests = AppManifest.find("app.id = ?1 ORDER BY creationPriority", app.id).list();
        
        for (AppManifest manifest : manifests) {
            String manifestKey = manifest.getManifestTypeName().toLowerCase();
            
            // Only generate config for required manifests or those with conditions met
            if (manifest.isRequired() || shouldCreateManifest(manifest, appConfig)) {
                ObjectNode manifestConfig = appConfig.putObject(manifestKey);
                manifestConfig.put("enabled", true);
                
                // Add default manifest configuration if available
                if (manifest.hasDefaultConfig()) {
                    manifestConfig.setAll((ObjectNode) manifest.defaultConfig);
                }
                
                // Add manifest-specific defaults
                addManifestDefaults(manifest.manifestType, manifestConfig, app);
            }
        }
    }
    
    private boolean shouldCreateManifest(AppManifest manifest, ObjectNode appConfig) {
        if (manifest.creationCondition == null) return true;
        
        // Simple condition evaluation (extend as needed)
        switch (manifest.creationCondition) {
            case "persistence.enabled":
                return appConfig.path("persistence").path("enabled").asBoolean(false);
            case "ingress.enabled":
                return appConfig.path("ingress").path("enabled").asBoolean(true); // Default true for apps
            case "hpa.enabled":
                return appConfig.path("hpa").path("enabled").asBoolean(false);
            default:
                return true;
        }
    }
    
    private void addManifestDefaults(ManifestType manifestType, ObjectNode config, App app) {
        switch (manifestType) {
            case DEPLOYMENT:
                config.put("replicaCount", 1);
                config.put("imagePullPolicy", "IfNotPresent");
                break;
            case SERVICE:
                config.put("type", "ClusterIP");
                break;
            case INGRESS:
                config.put("className", "nginx");
                config.put("host", app.name + ".lolmeida.com");
                ObjectNode annotations = config.putObject("annotations");
                annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
                annotations.put("cert-manager.io/cluster-issuer", "letsencrypt-prod");
                break;
            case PERSISTENT_VOLUME_CLAIM:
                config.put("accessMode", "ReadWriteOnce");
                config.put("size", "5Gi");
                break;
            case HPA:
                config.put("minReplicas", 1);
                config.put("maxReplicas", 5);
                config.put("targetCPUUtilizationPercentage", 70);
                break;
        }
    }
}
```

**Comandos:**
```bash
# Executar API
./mvnw quarkus:dev

# Testar endpoints
curl -X GET http://localhost:8080/api/config/environments
curl -X GET http://localhost:8080/api/config/environments/1/stacks/database/values | jq .
```

---

## 🎨 **FASE 3: FRONTEND (REACT)**

### **3.1. Project Setup**

```bash
# Criar projeto React
npx create-react-app config-frontend --template typescript
cd config-frontend

# Instalar dependências
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material
npm install axios react-router-dom
npm install @types/node
```

### **3.2. API Service**

```typescript
// src/services/api.ts
import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/config';

export interface Environment {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}

export interface Stack {
  id: number;
  name: string;
  enabled: boolean;
  description: string;
  apps: App[];
}

export interface App {
  id: number;
  name: string;
  displayName: string;
  enabled: boolean;
  category: string;
  deploymentPriority: number;
  defaultImageRepository: string;
  defaultImageTag: string;
  defaultConfig: any;
  dependencies: string[];
  healthCheckPath?: string;
  requiredManifests: AppManifest[];
}

export interface AppManifest {
  id: number;
  manifestType: string;
  required: boolean;
  creationPriority: number;
  creationCondition?: string;
  defaultConfig?: any;
  description: string;
}

export interface KubernetesResource {
  id: number;
  enabled: boolean;
  metadataName: string;
  metadataLabels: any;
}

class ConfigAPI {
  // Environments
  async getEnvironments(): Promise<Environment[]> {
    const response = await axios.get(`${API_BASE}/environments`);
    return response.data;
  }

  // Stacks
  async getStacks(envId: number): Promise<Stack[]> {
    const response = await axios.get(`${API_BASE}/environments/${envId}/stacks`);
    return response.data;
  }

  async updateStack(envId: number, stackName: string, stack: Partial<Stack>): Promise<Stack> {
    const response = await axios.put(`${API_BASE}/environments/${envId}/stacks/${stackName}`, stack);
    return response.data;
  }

  // Apps
  async getApps(envId: number, stackName: string): Promise<App[]> {
    const response = await axios.get(`${API_BASE}/environments/${envId}/stacks/${stackName}/apps`);
    return response.data;
  }

  async updateApp(envId: number, stackName: string, appName: string, app: Partial<App>): Promise<App> {
    const response = await axios.put(`${API_BASE}/environments/${envId}/stacks/${stackName}/apps/${appName}`, app);
    return response.data;
  }

  // App Manifests
  async getAppManifests(envId: number, stackName: string, appName: string): Promise<AppManifest[]> {
    const response = await axios.get(`${API_BASE}/environments/${envId}/stacks/${stackName}/apps/${appName}/manifests`);
    return response.data;
  }

  async updateAppManifest(envId: number, stackName: string, appName: string, manifestType: string, manifest: Partial<AppManifest>): Promise<AppManifest> {
    const response = await axios.put(`${API_BASE}/environments/${envId}/stacks/${stackName}/apps/${appName}/manifests/${manifestType}`, manifest);
    return response.data;
  }

  // Kubernetes Resources
  async getDeployments(envId: number, stackName: string, appName: string): Promise<any[]> {
    const response = await axios.get(`${API_BASE}/environments/${envId}/stacks/${stackName}/apps/${appName}/deployments`);
    return response.data;
  }

  // Values
  async generateValues(envId: number, stackName: string): Promise<any> {
    const response = await axios.get(`${API_BASE}/environments/${envId}/stacks/${stackName}/values`);
    return response.data;
  }

  // Deploy
  async deployStack(envId: number, stackName: string): Promise<any> {
    const response = await axios.post(`${API_BASE}/environments/${envId}/stacks/${stackName}/deploy`);
    return response.data;
  }
}

export const configAPI = new ConfigAPI();
```

### **3.3. Main App Component**

```tsx
// src/App.tsx
import React, { useState, useEffect } from 'react';
import { 
  AppBar, Toolbar, Typography, Container, Grid, 
  FormControl, InputLabel, Select, MenuItem 
} from '@mui/material';
import { StackManager } from './components/StackManager';
import { configAPI, Environment } from './services/api';

function App() {
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [selectedEnvId, setSelectedEnvId] = useState<number>(1);

  useEffect(() => {
    loadEnvironments();
  }, []);

  const loadEnvironments = async () => {
    try {
      const envs = await configAPI.getEnvironments();
      setEnvironments(envs);
    } catch (error) {
      console.error('Error loading environments:', error);
    }
  };

  return (
    <div className="App">
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            🗄️ Config Management
          </Typography>
          
          <FormControl variant="outlined" sx={{ minWidth: 120, ml: 2 }}>
            <InputLabel sx={{ color: 'white' }}>Environment</InputLabel>
            <Select
              value={selectedEnvId}
              onChange={(e) => setSelectedEnvId(Number(e.target.value))}
              label="Environment"
              sx={{ color: 'white', '.MuiOutlinedInput-notchedOutline': { borderColor: 'white' } }}
            >
              {environments.map(env => (
                <MenuItem key={env.id} value={env.id}>
                  {env.name.toUpperCase()}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ mt: 4 }}>
        <StackManager environmentId={selectedEnvId} />
      </Container>
    </div>
  );
}

export default App;
```

### **3.4. Stack Manager Component**

```tsx
// src/components/StackManager.tsx
import React, { useState, useEffect } from 'react';
import { 
  Grid, Card, CardContent, Typography, Switch, 
  FormControlLabel, Button, Accordion, AccordionSummary, 
  AccordionDetails, Alert
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import PlayArrowIcon from '@mui/icons-material/PlayArrow';
import { ServiceEditor } from './ServiceEditor';
import { configAPI, Stack, Service } from '../services/api';

interface Props {
  environmentId: number;
}

export const StackManager: React.FC<Props> = ({ environmentId }) => {
  const [stacks, setStacks] = useState<Stack[]>([]);
  const [apps, setApps] = useState<{[key: string]: App[]}>({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string>('');

  useEffect(() => {
    if (environmentId) {
      loadStacks();
    }
  }, [environmentId]);

  const loadStacks = async () => {
    try {
      const stacksData = await configAPI.getStacks(environmentId);
      setStacks(stacksData);
      
      // Load apps for each stack (sorted by deployment priority)
      const appsData: {[key: string]: App[]} = {};
      for (const stack of stacksData) {
        appsData[stack.name] = await configAPI.getApps(environmentId, stack.name);
      }
      setApps(appsData);
    } catch (error) {
      console.error('Error loading stacks:', error);
      setMessage('Error loading stacks');
    }
  };

  const handleStackToggle = async (stackName: string, enabled: boolean) => {
    try {
      await configAPI.updateStack(environmentId, stackName, { enabled });
      setMessage(`Stack ${stackName} ${enabled ? 'enabled' : 'disabled'}`);
      loadStacks(); // Reload
    } catch (error) {
      console.error('Error updating stack:', error);
      setMessage('Error updating stack');
    }
  };

  const handleDeploy = async (stackName: string) => {
    setLoading(true);
    try {
      await configAPI.deployStack(environmentId, stackName);
      setMessage(`Deployment started for ${stackName}`);
    } catch (error) {
      console.error('Error deploying stack:', error);
      setMessage('Error starting deployment');
    } finally {
      setLoading(false);
    }
  };

  const handleAppUpdate = async (stackName: string, appName: string, app: Partial<App>) => {
    try {
      await configAPI.updateApp(environmentId, stackName, appName, app);
      setMessage(`App ${app.displayName || appName} updated`);
      loadStacks(); // Reload
    } catch (error) {
      console.error('Error updating app:', error);
      setMessage('Error updating app');
    }
  };

  return (
    <div>
      {message && (
        <Alert severity="info" sx={{ mb: 2 }} onClose={() => setMessage('')}>
          {message}
        </Alert>
      )}

      <Grid container spacing={3}>
        {stacks.map(stack => (
          <Grid item xs={12} md={6} lg={4} key={stack.name}>
            <Card>
              <CardContent>
                <Typography variant="h5" component="div" gutterBottom>
                  📦 {stack.name.toUpperCase()}
                </Typography>
                
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  {stack.description}
                </Typography>

                <FormControlLabel
                  control={
                    <Switch 
                      checked={stack.enabled}
                      onChange={(e) => handleStackToggle(stack.name, e.target.checked)}
                    />
                  }
                  label="Enabled"
                />

                <Button 
                  variant="contained" 
                  startIcon={<PlayArrowIcon />}
                  onClick={() => handleDeploy(stack.name)}
                  disabled={loading || !stack.enabled}
                  fullWidth
                  sx={{ mt: 2 }}
                >
                  Deploy Stack
                </Button>

                {/* Apps Configuration */}
                {apps[stack.name] && apps[stack.name].map(app => (
                  <Accordion key={app.name} sx={{ mt: 2 }}>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography variant="h6">
                        {app.enabled ? '🟢' : '⚪'} {app.displayName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 2 }}>
                        {app.category} • Priority: {app.deploymentPriority} • Manifests: {app.requiredManifests?.length || 0}
                      </Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <AppEditor 
                        app={app}
                        onUpdate={(updatedApp) => 
                          handleAppUpdate(stack.name, app.name, updatedApp)
                        }
                      />
                    </AccordionDetails>
                  </Accordion>
                ))}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </div>
  );
};
```

### **3.5. App Editor Component**

```tsx
// src/components/AppEditor.tsx
import React, { useState } from 'react';
import { 
  TextField, Switch, FormControlLabel, Button, 
  Grid, Typography, Divider, Box, Chip
} from '@mui/material';
import { App, AppManifest } from '../services/api';

interface Props {
  app: App;
  onUpdate: (app: Partial<App>) => void;
}

export const AppEditor: React.FC<Props> = ({ app, onUpdate }) => {
  const [defaultConfig, setDefaultConfig] = useState(app.defaultConfig || {});
  const [enabled, setEnabled] = useState(app.enabled);
  const [imageTag, setImageTag] = useState(app.defaultImageTag);
  const [deploymentPriority, setDeploymentPriority] = useState(app.deploymentPriority);

  const handleConfigChange = (path: string, value: any) => {
    const newConfig = { ...defaultConfig };
    const keys = path.split('.');
    let current = newConfig;
    
    for (let i = 0; i < keys.length - 1; i++) {
      if (!current[keys[i]]) current[keys[i]] = {};
      current = current[keys[i]];
    }
    
    current[keys[keys.length - 1]] = value;
    setDefaultConfig(newConfig);
  };

  const handleSave = () => {
    onUpdate({ 
      enabled, 
      defaultConfig, 
      defaultImageTag: imageTag,
      deploymentPriority
    });
  };

  return (
    <Box>
      {/* App Header */}
      <Box sx={{ mb: 2 }}>
        <Typography variant="h5" gutterBottom>
          🚀 {app.displayName}
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
          <Chip label={app.category.toUpperCase()} color="primary" size="small" />
          <Chip label={`Priority: ${app.deploymentPriority}`} variant="outlined" size="small" />
          <Chip label={`Manifests: ${app.requiredManifests?.length || 0}`} variant="outlined" size="small" />
        </Box>

        {/* Dependencies */}
        {app.dependencies && app.dependencies.length > 0 && (
          <Box>
            <Typography variant="body2" color="text.secondary">
              Dependencies: {app.dependencies.join(', ')}
            </Typography>
          </Box>
        )}
      </Box>

      <FormControlLabel
        control={
          <Switch 
            checked={enabled}
            onChange={(e) => setEnabled(e.target.checked)}
          />
        }
        label="App Enabled"
      />

      <Divider sx={{ my: 2 }} />

      <Grid container spacing={2}>
        {/* App Configuration */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom>⚙️ App Configuration</Typography>
        </Grid>
        
        <Grid item xs={12} sm={4}>
          <TextField
            label="Deployment Priority"
            type="number"
            value={deploymentPriority}
            onChange={(e) => setDeploymentPriority(Number(e.target.value))}
            fullWidth
            size="small"
            helperText="Lower = deploys first"
          />
        </Grid>

        {/* Image Configuration */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>🐳 Docker Image</Typography>
        </Grid>
        
        <Grid item xs={12} sm={8}>
          <TextField
            label="Repository"
            value={app.defaultImageRepository}
            disabled
            fullWidth
            size="small"
            helperText="Image repository (read-only)"
          />
        </Grid>
        
        <Grid item xs={12} sm={4}>
          <TextField
            label="Tag"
            value={imageTag}
            onChange={(e) => setImageTag(e.target.value)}
            fullWidth
            size="small"
            helperText="Image tag version"
          />
        </Grid>

        {/* Resources Configuration */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>⚙️ Resources</Typography>
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <TextField
            label="Memory Limit"
            value={defaultConfig.resources?.limits?.memory || ''}
            onChange={(e) => handleConfigChange('resources.limits.memory', e.target.value)}
            fullWidth
            size="small"
            placeholder="512Mi"
          />
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <TextField
            label="CPU Limit"
            value={defaultConfig.resources?.limits?.cpu || ''}
            onChange={(e) => handleConfigChange('resources.limits.cpu', e.target.value)}
            fullWidth
            size="small"
            placeholder="200m"
          />
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <TextField
            label="Memory Request"
            value={defaultConfig.resources?.requests?.memory || ''}
            onChange={(e) => handleConfigChange('resources.requests.memory', e.target.value)}
            fullWidth
            size="small"
            placeholder="256Mi"
          />
        </Grid>
        
        <Grid item xs={12} sm={3}>
          <TextField
            label="CPU Request"
            value={defaultConfig.resources?.requests?.cpu || ''}
            onChange={(e) => handleConfigChange('resources.requests.cpu', e.target.value)}
            fullWidth
            size="small"
            placeholder="100m"
          />
        </Grid>

        {/* Health Checks */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>🏥 Health Checks</Typography>
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <TextField
            label="Health Check Path"
            value={app.healthCheckPath || ''}
            disabled
            fullWidth
            size="small"
            helperText="Liveness probe path (read-only)"
          />
        </Grid>

        {/* Manifests Overview */}
        <Grid item xs={12}>
          <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>📋 Required Manifests</Typography>
        </Grid>
        
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            {app.requiredManifests?.map((manifest: AppManifest) => (
              <Chip
                key={manifest.manifestType}
                label={manifest.manifestType}
                color={manifest.required ? "primary" : "secondary"}
                size="small"
                variant={manifest.required ? "filled" : "outlined"}
              />
            ))}
          </Box>
        </Grid>

        {/* Default Configuration (JSON editor placeholder) */}
        {defaultConfig && Object.keys(defaultConfig).length > 0 && (
          <>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>🔧 Advanced Configuration</Typography>
            </Grid>
            
            <Grid item xs={12}>
              <TextField
                label="Default Configuration (JSON)"
                value={JSON.stringify(defaultConfig, null, 2)}
                onChange={(e) => {
                  try {
                    setDefaultConfig(JSON.parse(e.target.value));
                  } catch (err) {
                    // Invalid JSON, keep as string for editing
                  }
                }}
                multiline
                rows={8}
                fullWidth
                size="small"
                helperText="Raw JSON configuration for the app"
              />
            </Grid>
          </>
        )}
      </Grid>

      <Button 
        variant="contained" 
        onClick={handleSave}
        fullWidth
        sx={{ mt: 3 }}
      >
        💾 Save App Configuration
      </Button>
    </Box>
  );
};
```

**Comandos:**
```bash
# Executar frontend
npm start

# Aplicação acessível em http://localhost:3000
```

---

## 🔧 **FASE 4: ENHANCED DEPLOY SCRIPT**

### **4.1. deploy-n8n.sh v2.0**

```bash
#!/bin/bash
# deploy-n8n.sh v2.0 - Database-driven deployment

set -eo pipefail

# Configuration
CONFIG_API_URL="${CONFIG_API_URL:-http://localhost:8080/api/config}"
VPS_HOST="${VPS_HOST:-n8n}"
REPO_URL="https://github.com/lolmeida/n8n-chart.git"
TEMP_DIR="/tmp/deploy-$(date +%s)"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${CYAN}[$(date '+%H:%M:%S')]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1" >&2; }
success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARNING]${NC} $1"; }

show_usage() {
    echo "Usage: $0 <release-name> [environment] <stack-name>"
    echo ""
    echo "Examples:"
    echo "  $0 database prod database      # Deploy database stack to prod"
    echo "  $0 monitoring staging monitoring # Deploy monitoring stack to staging"
    echo "  $0 apps dev apps               # Deploy apps stack to dev"
    echo ""
    echo "Available stacks: database, monitoring, apps"
    echo "Available environments: prod, staging, dev"
}

# Get values from API
get_values_from_api() {
    local environment=$1
    local stack_name=$2
    local temp_file="/tmp/values-${environment}-${stack_name}.json"
    
    log "🔄 Fetching configuration from API..."
    
    # Get environment ID
    ENV_ID=$(curl -s "${CONFIG_API_URL}/environments" | jq -r ".[] | select(.name==\"${environment}\") | .id")
    
    if [ -z "$ENV_ID" ] || [ "$ENV_ID" == "null" ]; then
        error "Environment '${environment}' not found"
        return 1
    fi
    
    # Get values from API
    if ! curl -s "${CONFIG_API_URL}/environments/${ENV_ID}/stacks/${stack_name}/values" > "$temp_file"; then
        error "Failed to fetch configuration from API"
        return 1
    fi
    
    # Validate JSON
    if ! jq empty "$temp_file" 2>/dev/null; then
        error "Invalid JSON returned from API"
        return 1
    fi
    
    success "Configuration loaded from API"
    echo "$temp_file"
}

# Notify deployment status to API
notify_deployment_status() {
    local env_id=$1
    local stack=$2
    local status=$3
    local log_message="$4"
    
    curl -s -X POST "${CONFIG_API_URL}/environments/${env_id}/stacks/${stack}/deploy" \
        -H "Content-Type: application/json" \
        -d "{
            \"status\": \"${status}\",
            \"deployed_by\": \"$(whoami)\",
            \"deploy_log\": \"${log_message}\"
        }" > /dev/null || true
}

# Deploy with API integration
deploy_with_api() {
    local release_name=$1
    local environment=$2
    local stack_name=$3
    local namespace=${4:-$environment}
    
    log "🚀 Starting deployment: ${BLUE}$release_name${NC} ($environment/$stack_name)"
    
    # Get environment ID for notifications
    ENV_ID=$(curl -s "${CONFIG_API_URL}/environments" | jq -r ".[] | select(.name==\"${environment}\") | .id")
    
    # Notify deployment start
    notify_deployment_status "$ENV_ID" "$stack_name" "RUNNING" "Deployment started"
    
    # Get configuration from API
    VALUES_FILE=$(get_values_from_api "$environment" "$stack_name")
    if [ $? -ne 0 ]; then
        notify_deployment_status "$ENV_ID" "$stack_name" "FAILED" "Failed to get configuration from API"
        return 1
    fi
    
    # Convert JSON to YAML for Helm
    YAML_FILE="${VALUES_FILE}.yaml"
    if ! yq eval -P "$VALUES_FILE" > "$YAML_FILE"; then
        error "Failed to convert JSON to YAML"
        notify_deployment_status "$ENV_ID" "$stack_name" "FAILED" "JSON to YAML conversion failed"
        return 1
    fi
    
    log "📦 Deploying to remote server..."
    
    # Deploy on remote server
    if ssh "$VPS_HOST" bash << EOF
        set -eo pipefail
        
        # Cleanup
        rm -rf "$TEMP_DIR" 2>/dev/null || true
        
        # Clone repository
        if ! git clone --quiet --branch main "$REPO_URL" "$TEMP_DIR"; then
            echo "❌ Failed to clone repository"
            exit 1
        fi
        
        # Navigate to chart directory
        cd "$TEMP_DIR/k8s"
        
        # Copy values file to server
        cat > custom-values.yaml << 'EOL'
$(cat "$YAML_FILE")
EOL
        
        # Deploy with Helm
        if microk8s helm3 upgrade --install "$release_name" . \
            -f custom-values.yaml \
            --namespace "$namespace" \
            --create-namespace \
            --wait --timeout=300s; then
            echo "✅ Deployment successful"
        else
            echo "❌ Deployment failed"
            exit 1
        fi
        
        # Cleanup
        rm -rf "$TEMP_DIR"
EOF
    then
        success "🎉 Deployment completed successfully!"
        notify_deployment_status "$ENV_ID" "$stack_name" "SUCCESS" "Deployment completed successfully"
        
        # Show useful commands
        log "🔧 Useful commands:"
        log "   microk8s kubectl get all -n $namespace"
        log "   microk8s helm3 list -n $namespace"
        
        case "$stack_name" in
            "database")
                log "   🗄️ Database services deployed"
                ;;
            "monitoring")
                log "   🌐 Grafana: https://grafana.lolmeida.com"
                log "   🌐 Prometheus: https://prometheus.lolmeida.com"
                ;;
            "apps")
                log "   🌐 N8N: https://n8n.lolmeida.com"
                log "   🌐 Peah-DB: https://peah-db.lolmeida.com"
                ;;
        esac
    else
        error "❌ Deployment failed!"
        notify_deployment_status "$ENV_ID" "$stack_name" "FAILED" "Helm deployment failed"
        return 1
    fi
    
    # Cleanup local files
    rm -f "$VALUES_FILE" "$YAML_FILE"
}

# Validate parameters
RELEASE_NAME=${1:-""}
ENVIRONMENT=${2:-"prod"}
STACK_NAME=${3:-""}

if [ -z "$RELEASE_NAME" ] || [ -z "$STACK_NAME" ]; then
    error "Missing required parameters!"
    show_usage
    exit 1
fi

# Validate stack name
case "$STACK_NAME" in
    "database"|"monitoring"|"apps") ;;
    *) 
        error "Invalid stack name: $STACK_NAME"
        echo "Valid stacks: database, monitoring, apps"
        exit 1
        ;;
esac

# Validate environment
case "$ENVIRONMENT" in
    "prod"|"staging"|"dev") ;;
    *) 
        error "Invalid environment: $ENVIRONMENT"
        echo "Valid environments: prod, staging, dev"
        exit 1
        ;;
esac

# Check API connectivity
log "🔗 Checking API connectivity..."
if ! curl -s "$CONFIG_API_URL/environments" >/dev/null; then
    error "Cannot connect to Config API at $CONFIG_API_URL"
    exit 1
fi

# Check SSH connection
log "🔗 Checking SSH connection to $VPS_HOST..."
if ! ssh -o ConnectTimeout=5 "$VPS_HOST" "echo 'OK'" >/dev/null 2>&1; then
    error "Cannot connect to $VPS_HOST"
    exit 1
fi

# Execute deployment
echo "========================================"
deploy_with_api "$RELEASE_NAME" "$ENVIRONMENT" "$STACK_NAME"
```

**Permissões:**
```bash
chmod +x deploy-n8n.sh
```

---

## 🧪 **FASE 5: TESTING & VALIDATION**

### **5.1. API Testing**

```bash
# Test environments
curl -X GET http://localhost:8080/api/config/environments | jq .

# Test stacks
curl -X GET http://localhost:8080/api/config/environments/1/stacks | jq .

# Test apps
curl -X GET http://localhost:8080/api/config/environments/1/stacks/database/apps | jq .

# Test app manifests
curl -X GET http://localhost:8080/api/config/environments/1/stacks/database/apps/postgresql/manifests | jq .

# Test values generation
curl -X GET http://localhost:8080/api/config/environments/1/stacks/database/values | jq .

# Test app update
curl -X PUT http://localhost:8080/api/config/environments/1/stacks/database/apps/postgresql \
  -H "Content-Type: application/json" \
  -d '{"enabled": true, "defaultImageTag": "15", "deploymentPriority": 5}' | jq .

# Test manifest update
curl -X PUT http://localhost:8080/api/config/environments/1/stacks/database/apps/postgresql/manifests/DEPLOYMENT \
  -H "Content-Type: application/json" \
  -d '{"required": true, "defaultConfig": {"replicaCount": 2}}' | jq .
```

### **5.2. Frontend Testing**

```bash
# Build for production
npm run build

# Test production build
npm install -g serve
serve -s build -l 3000
```

### **5.3. Integration Testing**

```bash
# Full workflow test
# 1. Start all services
docker-compose up -d
./mvnw quarkus:dev &
npm start &

# 2. Wait for services to start
sleep 10

# 3. Test API endpoints
curl http://localhost:8080/api/config/environments

# 4. Test frontend
curl http://localhost:3000

# 5. Test deployment (dry run)
CONFIG_API_URL=http://localhost:8080/api/config ./deploy-n8n.sh database-test dev database
```

---

## 🚀 **FASE 6: DEPLOYMENT TO PRODUCTION**

### **6.1. Production Configuration**

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  config-db:
    image: postgres:14
    environment:
      POSTGRES_DB: config
      POSTGRES_USER: config
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    restart: unless-stopped

  config-api:
    build: ./config-api
    environment:
      QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://config-db:5432/config
      QUARKUS_DATASOURCE_USERNAME: config
      QUARKUS_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    depends_on:
      - config-db
    restart: unless-stopped

  config-frontend:
    build: ./config-frontend
    environment:
      REACT_APP_API_URL: https://config-api.lolmeida.com/api/config
    ports:
      - "3000:80"
    restart: unless-stopped

volumes:
  postgres_data:
```

### **6.2. Environment Variables**

```bash
# .env.prod
DB_PASSWORD=super-secure-password-change-me
API_URL=https://config-api.lolmeida.com
FRONTEND_URL=https://config-ui.lolmeida.com
```

### **6.3. Nginx Configuration**

```nginx
# /etc/nginx/sites-available/config-api.lolmeida.com
server {
    listen 80;
    server_name config-api.lolmeida.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name config-api.lolmeida.com;

    ssl_certificate /etc/letsencrypt/live/config-api.lolmeida.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/config-api.lolmeida.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# /etc/nginx/sites-available/config-ui.lolmeida.com  
server {
    listen 80;
    server_name config-ui.lolmeida.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name config-ui.lolmeida.com;

    ssl_certificate /etc/letsencrypt/live/config-ui.lolmeida.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/config-ui.lolmeida.com/privkey.pem;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### **6.4. Production Deployment**

```bash
# Deploy to production
scp -r . root@config-server:/opt/config-system/
ssh root@config-server "cd /opt/config-system && docker-compose -f docker-compose.prod.yml up -d"

# Setup nginx
ssh root@config-server "ln -sf /opt/config-system/nginx-configs/* /etc/nginx/sites-available/"
ssh root@config-server "ln -sf /etc/nginx/sites-available/config-*.lolmeida.com /etc/nginx/sites-enabled/"
ssh root@config-server "nginx -t && systemctl reload nginx"

# Setup SSL
ssh root@config-server "certbot --nginx -d config-api.lolmeida.com -d config-ui.lolmeida.com"
```

---

## 📊 **FASE 7: MONITORING & MAINTENANCE**

### **7.1. Health Checks**

```bash
# API health
curl -f https://config-api.lolmeida.com/q/health || echo "API DOWN"

# Database connectivity
curl -f https://config-api.lolmeida.com/api/config/environments || echo "DB DOWN"

# Frontend health
curl -f https://config-ui.lolmeida.com || echo "FRONTEND DOWN"
```

### **7.2. Backup Strategy**

```bash
# Database backup
docker exec config-api-config-db-1 pg_dump -U config config | gzip > backup-$(date +%Y%m%d).sql.gz

# Configuration export
curl -s https://config-api.lolmeida.com/api/config/environments | jq . > environments-backup-$(date +%Y%m%d).json
```

### **7.3. Log Monitoring**

```bash
# API logs
docker logs -f config-api-config-api-1

# Database logs
docker logs -f config-api-config-db-1

# Frontend logs
docker logs -f config-api-config-frontend-1
```

---

## 🎯 **COMANDOS ESSENCIAIS**

### **Development**
```bash
# Start full stack
docker-compose up -d config-db
cd config-api && ./mvnw quarkus:dev &
cd config-frontend && npm start &

# Reset database
docker-compose down -v && docker-compose up -d config-db
```

### **Testing**
```bash
# API tests
curl http://localhost:8080/api/config/environments | jq .

# Deploy test
./deploy-n8n.sh database-test dev database
```

### **Production**
```bash
# Deploy stack
./deploy-n8n.sh database prod database
./deploy-n8n.sh monitoring prod monitoring
./deploy-n8n.sh apps prod apps

# Monitor deployments
kubectl get pods -n prod
kubectl get deployments -n prod
```

### **Maintenance**
```bash
# Backup
docker exec config-db pg_dump -U config config > backup.sql

# Restore
docker exec -i config-db psql -U config config < backup.sql

# Logs
docker logs -f config-api
```

---

## ✅ **CHECKLIST DE IMPLEMENTAÇÃO**

### **Fase 1: Database (1-2 dias)**
- [ ] PostgreSQL setup
- [ ] Schema creation
- [ ] Seed data insertion
- [ ] Basic connectivity test

### **Fase 2: API (3-4 dias)**
- [ ] Quarkus project setup
- [ ] Core entity classes (Environment, Stack, App, AppManifest)
- [ ] Kubernetes resource entities (9 classes)
- [ ] REST endpoints for Apps and Manifests
- [ ] Enhanced Values generation service
- [ ] API testing

### **Fase 3: Frontend (4-5 dias)**
- [ ] React project setup
- [ ] API service layer
- [ ] Environment selector
- [ ] Stack manager component
- [ ] Service editor component
- [ ] Frontend testing

### **Fase 4: Deploy Script (1-2 dias)**
- [ ] Enhanced deploy script
- [ ] API integration
- [ ] Error handling
- [ ] Status notifications

### **Fase 5: Testing (2-3 dias)**
- [ ] Unit tests
- [ ] Integration tests
- [ ] End-to-end testing
- [ ] Performance testing

### **Fase 6: Production (2-3 dias)**
- [ ] Production configuration
- [ ] Nginx setup
- [ ] SSL certificates
- [ ] Monitoring setup

### **Total: ~15-20 dias de desenvolvimento**

---

## 🎯 **PRÓXIMOS PASSOS**

1. **✅ Setup inicial** - Database + API básica
2. **🎨 Frontend MVP** - Interface funcional básica
3. **🔧 Deploy integration** - Script enhanced
4. **🧪 Testing completo** - Validação end-to-end
5. **🚀 Production deploy** - Sistema em produção
6. **📈 Otimizações** - Performance e features avançadas

---

**📋 Status**: 🎯 **ESTRUTURA DE DADOS COMPLETA**  
**⏱️ Timeline**: 15-20 dias  
**👥 Team**: DevOps + Developer  
**📅 Entities**: ✅ **15 Classes Java criadas**  
**📅 Start**: Quando aprovado

### **✅ CONCLUÍDO:**
- 📊 **Estrutura de dados completa** definida
- 🏗️ **15 Java Entity classes** criadas
- 📋 **Apps e Manifestos** mapeados
- 🎯 **Seed Data** com exemplos reais
- 🔄 **Hierarquia Environment→Stack→App→Manifest** implementada

### **🎯 PRÓXIMOS PASSOS:**
1. **🗄️ SQL Schema generation** a partir das entities
2. **🚀 API implementation** com endpoints CRUD
3. **🎨 Frontend development** com nova estrutura
4. **🧪 Testing completo** do fluxo
5. **📦 Deploy integration** enhanced

> 🚀 **Guia completo para implementação da solução database-driven com Apps e Manifestos K8s!** 