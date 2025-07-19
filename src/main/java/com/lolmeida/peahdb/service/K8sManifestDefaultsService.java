package com.lolmeida.peahdb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lolmeida.peahdb.entity.k8s.App;
import com.lolmeida.peahdb.entity.k8s.AppManifest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

/**
 * Service to manage default Kubernetes manifests for each service category.
 * Java equivalent of the frontend k8sManifestDefaults.ts
 */
@ApplicationScoped
public class K8sManifestDefaultsService {

    @Inject
    ObjectMapper objectMapper;

    // Manifests defaults for each category
    private static final Map<String, List<ManifestDefault>> MANIFEST_DEFAULTS = new HashMap<>();
    
    static {
        initializeDefaults();
    }

    /**
     * Represents a default manifest configuration for a service category
     */
    public static class ManifestDefault {
        public AppManifest.ManifestType manifestType;
        public boolean required;
        public int creationPriority;
        public String description;
        public String creationCondition;
        public Map<String, Object> defaultConfig;

        public ManifestDefault(AppManifest.ManifestType manifestType, boolean required, int creationPriority, 
                              String description, String creationCondition, Map<String, Object> defaultConfig) {
            this.manifestType = manifestType;
            this.required = required;
            this.creationPriority = creationPriority;
            this.description = description;
            this.creationCondition = creationCondition;
            this.defaultConfig = defaultConfig != null ? defaultConfig : new HashMap<>();
        }

        public ManifestDefault(AppManifest.ManifestType manifestType, boolean required, int creationPriority, String description) {
            this(manifestType, required, creationPriority, description, null, new HashMap<>());
        }
    }

    /**
     * Initialize default manifests for each category
     */
    private static void initializeDefaults() {
        // 🗄️ Database Services (PostgreSQL, Redis, etc.)
        List<ManifestDefault> databaseDefaults = Arrays.asList(
            new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10, 
                "Database deployment with persistence and health checks",
                null, Map.of(
                    "replicaCount", 1,
                    "restartPolicy", "Always", 
                    "imagePullPolicy", "IfNotPresent"
                )),
            new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
                "Internal service for database access",
                null, Map.of(
                    "type", "ClusterIP",
                    "sessionAffinity", "None"
                )),
            new ManifestDefault(AppManifest.ManifestType.PERSISTENT_VOLUME_CLAIM, true, 5,
                "Persistent storage for database data", 
                "persistence.enabled", Map.of(
                    "accessMode", "ReadWriteOnce",
                    "size", "10Gi",
                    "storageClass", "default"
                )),
            new ManifestDefault(AppManifest.ManifestType.SECRET, true, 1,
                "Database credentials and configuration secrets",
                "auth.enabled", Map.of(
                    "type", "Opaque",
                    "immutable", false
                )),
            new ManifestDefault(AppManifest.ManifestType.CONFIG_MAP, false, 15,
                "Database configuration files",
                null, Map.of("immutable", false))
        );

        // 📊 Monitoring Services (Prometheus, Grafana, etc.)
        List<ManifestDefault> monitoringDefaults = Arrays.asList(
            new ManifestDefault(AppManifest.ManifestType.SERVICE_ACCOUNT, true, 1,
                "Service account for monitoring permissions",
                null, Map.of("automountServiceAccountToken", true)),
            new ManifestDefault(AppManifest.ManifestType.CLUSTER_ROLE, true, 2,
                "Cluster role for monitoring access across namespaces",
                null, Map.of(
                    "createBinding", true,
                    "rules", Arrays.asList(
                        Map.of("apiGroups", Arrays.asList(""),
                               "resources", Arrays.asList("nodes", "services", "endpoints", "pods"),
                               "verbs", Arrays.asList("get", "list", "watch")),
                        Map.of("apiGroups", Arrays.asList("extensions"),
                               "resources", Arrays.asList("ingresses"),
                               "verbs", Arrays.asList("get", "list", "watch"))
                    )
                )),
            new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10,
                "Monitoring service deployment",
                null, Map.of("replicaCount", 1, "restartPolicy", "Always")),
            new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
                "Service for monitoring access",
                null, Map.of("type", "ClusterIP")),
            new ManifestDefault(AppManifest.ManifestType.INGRESS, false, 30,
                "External access to monitoring dashboard",
                "ingress.enabled", Map.of(
                    "className", "nginx",
                    "tls", Map.of("enabled", true),
                    "annotations", Map.of(
                        "nginx.ingress.kubernetes.io/ssl-redirect", "true",
                        "cert-manager.io/cluster-issuer", "letsencrypt-prod"
                    )
                )),
            new ManifestDefault(AppManifest.ManifestType.PERSISTENT_VOLUME_CLAIM, false, 5,
                "Storage for monitoring data retention",
                "persistence.enabled", Map.of(
                    "accessMode", "ReadWriteOnce", 
                    "size", "20Gi"
                )),
            new ManifestDefault(AppManifest.ManifestType.SECRET, false, 3,
                "Authentication credentials for monitoring access",
                "auth.enabled", Map.of("type", "Opaque")),
            new ManifestDefault(AppManifest.ManifestType.CONFIG_MAP, true, 8,
                "Monitoring configuration files",
                null, Map.of("immutable", false))
        );

        // 🤖 Automation Services (N8N, etc.)
        List<ManifestDefault> automationDefaults = Arrays.asList(
            new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10,
                "Automation platform deployment",
                null, Map.of("replicaCount", 1, "restartPolicy", "Always")),
            new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
                "Internal service for automation platform",
                null, Map.of("type", "ClusterIP")),
            new ManifestDefault(AppManifest.ManifestType.INGRESS, true, 30,
                "External access to automation interface",
                null, Map.of(
                    "className", "nginx",
                    "tls", Map.of("enabled", true),
                    "annotations", Map.of(
                        "nginx.ingress.kubernetes.io/ssl-redirect", "true",
                        "cert-manager.io/cluster-issuer", "letsencrypt-prod"
                    )
                )),
            new ManifestDefault(AppManifest.ManifestType.PERSISTENT_VOLUME_CLAIM, false, 5,
                "Storage for automation workflows and data",
                "persistence.enabled", Map.of(
                    "accessMode", "ReadWriteOnce",
                    "size", "5Gi"
                )),
            new ManifestDefault(AppManifest.ManifestType.SECRET, true, 1,
                "Authentication and encryption keys",
                null, Map.of("type", "Opaque", "immutable", false)),
            new ManifestDefault(AppManifest.ManifestType.CONFIG_MAP, false, 15,
                "Automation platform configuration",
                null, Map.of("immutable", false))
        );

        // 🚀 API Services (REST APIs, GraphQL, etc.)
        List<ManifestDefault> apiDefaults = Arrays.asList(
            new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10,
                "API service deployment with health checks",
                null, Map.of(
                    "replicaCount", 2, // APIs typically need more replicas
                    "restartPolicy", "Always",
                    "strategy", Map.of(
                        "type", "RollingUpdate",
                        "rollingUpdate", Map.of(
                            "maxSurge", 1,
                            "maxUnavailable", 0
                        )
                    )
                )),
            new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
                "Internal service for API access",
                null, Map.of("type", "ClusterIP", "sessionAffinity", "None")),
            new ManifestDefault(AppManifest.ManifestType.INGRESS, true, 30,
                "External API access with rate limiting",
                null, Map.of(
                    "className", "nginx",
                    "tls", Map.of("enabled", true),
                    "annotations", Map.of(
                        "nginx.ingress.kubernetes.io/ssl-redirect", "true",
                        "cert-manager.io/cluster-issuer", "letsencrypt-prod",
                        "nginx.ingress.kubernetes.io/rate-limit", "100",
                        "nginx.ingress.kubernetes.io/rate-limit-window", "1m"
                    )
                )),
            new ManifestDefault(AppManifest.ManifestType.HPA, false, 40,
                "Horizontal Pod Autoscaler for API scaling",
                "hpa.enabled", Map.of(
                    "minReplicas", 2,
                    "maxReplicas", 10,
                    "targetCPUUtilizationPercentage", 70,
                    "targetMemoryUtilizationPercentage", 80
                )),
            new ManifestDefault(AppManifest.ManifestType.SECRET, true, 1,
                "API keys, JWT secrets, and database credentials",
                null, Map.of("type", "Opaque", "immutable", false)),
            new ManifestDefault(AppManifest.ManifestType.CONFIG_MAP, true, 8,
                "API configuration and environment variables",
                null, Map.of("immutable", false)),
            new ManifestDefault(AppManifest.ManifestType.SERVICE_ACCOUNT, false, 2,
                "Service account for API permissions",
                "serviceAccount.create", Map.of("automountServiceAccountToken", true))
        );

        // 🔧 Default fallback for unknown categories
        List<ManifestDefault> defaultDefaults = Arrays.asList(
            new ManifestDefault(AppManifest.ManifestType.DEPLOYMENT, true, 10,
                "Application deployment",
                null, Map.of("replicaCount", 1, "restartPolicy", "Always")),
            new ManifestDefault(AppManifest.ManifestType.SERVICE, true, 20,
                "Internal service access",
                null, Map.of("type", "ClusterIP"))
        );

        MANIFEST_DEFAULTS.put("database", databaseDefaults);
        MANIFEST_DEFAULTS.put("monitoring", monitoringDefaults);  
        MANIFEST_DEFAULTS.put("automation", automationDefaults);
        MANIFEST_DEFAULTS.put("api", apiDefaults);
        MANIFEST_DEFAULTS.put("default", defaultDefaults);
    }

    /**
     * Get default manifests for a category
     */
    public List<ManifestDefault> getDefaultManifestsForCategory(String category) {
        return MANIFEST_DEFAULTS.getOrDefault(category.toLowerCase(), MANIFEST_DEFAULTS.get("default"));
    }

    /**
     * Get all manifest types used
     */
    public Set<String> getAllManifestTypes() {
        Set<String> types = new HashSet<>();
        MANIFEST_DEFAULTS.values().forEach(manifests -> 
            manifests.forEach(manifest -> types.add(manifest.manifestType.name()))
        );
        return types;
    }

    /**
     * Get manifest by type and category
     */
    public Optional<ManifestDefault> getManifestDefault(String category, AppManifest.ManifestType manifestType) {
        List<ManifestDefault> manifests = getDefaultManifestsForCategory(category);
        return manifests.stream()
                .filter(m -> m.manifestType == manifestType)
                .findFirst();
    }

    /**
     * Check if a manifest should be created based on conditions
     */
    public boolean shouldCreateManifest(ManifestDefault manifest, JsonNode appConfig) {
        if (manifest.creationCondition == null || manifest.creationCondition.isEmpty()) {
            return manifest.required;
        }

        // Simple condition evaluation
        String condition = manifest.creationCondition;
        
        if (condition.contains(".")) {
            String[] parts = condition.split("\\.");
            if (parts.length == 2) {
                String parent = parts[0];
                String child = parts[1];
                return appConfig.path(parent).path(child).asBoolean(false);
            }
        }
        
        return appConfig.path(condition).asBoolean(false);
    }

    /**
     * Generate app manifests based on category and configuration
     */
    public List<AppManifest> generateAppManifests(App app, JsonNode appConfig) {
        List<ManifestDefault> defaults = getDefaultManifestsForCategory(app.category);
        List<AppManifest> manifests = new ArrayList<>();
        
        for (ManifestDefault defaultManifest : defaults) {
            if (shouldCreateManifest(defaultManifest, appConfig)) {
                AppManifest manifest = new AppManifest();
                manifest.app = app;
                manifest.manifestType = defaultManifest.manifestType;
                manifest.required = defaultManifest.required;
                manifest.creationPriority = defaultManifest.creationPriority;
                manifest.description = defaultManifest.description;
                manifest.creationCondition = defaultManifest.creationCondition;
                
                // Convert defaultConfig to JsonNode
                if (!defaultManifest.defaultConfig.isEmpty()) {
                    manifest.defaultConfig = objectMapper.valueToTree(defaultManifest.defaultConfig);
                }
                
                manifests.add(manifest);
            }
        }
        
        return manifests;
    }

    /**
     * Enhanced condition evaluation with support for complex auth configurations
     */
    public boolean evaluateManifestCondition(String condition, JsonNode appConfig) {
        if (condition == null || condition.isEmpty()) {
            return true;
        }

        switch (condition) {
            case "auth.enabled":
                return appConfig.path("auth").path("enabled").asBoolean(false);
            
            case "persistence.enabled": 
                return appConfig.path("persistence").path("enabled").asBoolean(false);
            
            case "ingress.enabled":
                return appConfig.path("ingress").path("enabled").asBoolean(true); // Default true for most apps
            
            case "hpa.enabled":
                return appConfig.path("hpa").path("enabled").asBoolean(false);
            
            case "serviceAccount.create":
                return appConfig.path("serviceAccount").path("create").asBoolean(false);
            
            default:
                // Generic path evaluation
                if (condition.contains(".")) {
                    String[] parts = condition.split("\\.");
                    JsonNode current = appConfig;
                    for (String part : parts) {
                        current = current.path(part);
                    }
                    return current.asBoolean(false);
                }
                return appConfig.path(condition).asBoolean(false);
        }
    }

    /**
     * Get default configuration for auth based on app category and auth type
     */
    public JsonNode getDefaultAuthConfig(String category, String authType) {
        ObjectNode authConfig = objectMapper.createObjectNode();
        authConfig.put("enabled", true);
        authConfig.put("type", authType);

        switch (category.toLowerCase()) {
            case "database":
                switch (authType) {
                    case "password":
                        authConfig.put("username", "admin");
                        authConfig.put("database", "main");
                        authConfig.put("existingSecret", category + "-secret");
                        authConfig.put("allowEmptyPassword", false);
                        break;
                }
                break;
            
            case "monitoring":
                switch (authType) {
                    case "basic":
                        authConfig.put("adminUser", "admin");
                        authConfig.put("existingSecret", category + "-secret");
                        authConfig.put("autoAssignOrgRole", "Viewer");
                        authConfig.put("allowSignUp", false);
                        break;
                }
                break;
            
            case "automation":
                switch (authType) {
                    case "email":
                        ObjectNode defaultUser = authConfig.putObject("defaultUser");
                        defaultUser.put("email", "admin@" + category + ".local");
                        defaultUser.put("firstName", "Admin");
                        defaultUser.put("lastName", "User");
                        authConfig.put("existingSecret", category + "-secret");
                        authConfig.put("enablePublicAPI", true);
                        break;
                }
                break;
            
            case "api":
                switch (authType) {
                    case "jwt":
                        ObjectNode jwt = authConfig.putObject("jwt");
                        jwt.put("issuer", category + "-api");
                        jwt.put("expirationTime", "24h");
                        authConfig.put("existingSecret", category + "-auth-secret");
                        
                        ObjectNode cors = authConfig.putObject("cors");
                        cors.put("enabled", true);
                        ArrayNode allowedOrigins = cors.putArray("allowedOrigins");
                        allowedOrigins.add("*");
                        
                        ObjectNode rateLimit = authConfig.putObject("rateLimit");
                        rateLimit.put("enabled", true);
                        rateLimit.put("requestsPerMinute", 100);
                        break;
                }
                break;
        }

        return authConfig;
    }
} 