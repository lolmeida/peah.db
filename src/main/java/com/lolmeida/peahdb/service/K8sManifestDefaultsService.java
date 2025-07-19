package com.lolmeida.peahdb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lolmeida.peahdb.entity.k8s.App;
import com.lolmeida.peahdb.entity.k8s.AppManifest;
import com.lolmeida.peahdb.entity.k8s.AuthDefault;
import com.lolmeida.peahdb.entity.k8s.ManifestDefault;
import com.lolmeida.peahdb.entity.k8s.ServiceCategory;
import com.lolmeida.peahdb.repository.ManifestDefaultsRepository;
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

    @Inject
    ManifestDefaultsRepository repository;

    // Cache for frequently accessed data
    private final Map<String, List<ManifestDefaultEntry>> manifestCache = new HashMap<>();
    private final Map<String, List<AuthDefault>> authCache = new HashMap<>();

    /**
     * Represents a default manifest configuration for a service category
     * This is a DTO wrapper around the database entity
     */
    public static class ManifestDefaultEntry {
        public AppManifest.ManifestType manifestType;
        public boolean required;
        public int creationPriority;
        public String description;
        public String creationCondition;
        public Map<String, Object> defaultConfig;

        // Constructor from database entity
        public ManifestDefaultEntry(ManifestDefault entity, ObjectMapper mapper) {
            this.manifestType = entity.getManifestType();
            this.required = entity.isRequired();
            this.creationPriority = entity.getCreationPriority();
            this.description = entity.getDescription();
            this.creationCondition = entity.getCreationCondition();
            
            // Convert JsonNode to Map for compatibility
            this.defaultConfig = new HashMap<>();
            if (entity.hasDefaultConfig()) {
                try {
                    this.defaultConfig = mapper.convertValue(entity.getDefaultConfig(), Map.class);
                } catch (Exception e) {
                    // Log warning and use empty config
                    this.defaultConfig = new HashMap<>();
                }
            }
        }

        // Legacy constructor for backwards compatibility
        public ManifestDefaultEntry(AppManifest.ManifestType manifestType, boolean required, int creationPriority, 
                                  String description, String creationCondition, Map<String, Object> defaultConfig) {
            this.manifestType = manifestType;
            this.required = required;
            this.creationPriority = creationPriority;
            this.description = description;
            this.creationCondition = creationCondition;
            this.defaultConfig = defaultConfig != null ? defaultConfig : new HashMap<>();
        }

        public ManifestDefaultEntry(AppManifest.ManifestType manifestType, boolean required, int creationPriority, String description) {
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
     * Get default manifests for a category from database
     */
    public List<ManifestDefaultEntry> getDefaultManifestsForCategory(String category) {
        // Check cache first
        String cacheKey = category.toLowerCase();
        if (manifestCache.containsKey(cacheKey)) {
            return manifestCache.get(cacheKey);
        }
        
        // Load from database
        List<ManifestDefault> dbDefaults = repository.findManifestDefaultsByCategory(cacheKey);
        List<ManifestDefaultEntry> entries = new ArrayList<>();
        
        for (ManifestDefault dbDefault : dbDefaults) {
            entries.add(new ManifestDefaultEntry(dbDefault, objectMapper));
        }
        
        // If no defaults found, try fallback to "default" category
        if (entries.isEmpty() && !"default".equals(cacheKey)) {
            List<ManifestDefault> fallbackDefaults = repository.findManifestDefaultsByCategory("default");
            for (ManifestDefault dbDefault : fallbackDefaults) {
                entries.add(new ManifestDefaultEntry(dbDefault, objectMapper));
            }
        }
        
        // Cache the result
        manifestCache.put(cacheKey, entries);
        return entries;
    }

    /**
     * Get all manifest types used from database
     */
    public Set<String> getAllManifestTypes() {
        List<String> types = repository.getAllManifestTypes();
        return new HashSet<>(types);
    }

    /**
     * Get manifest by type and category from database
     */
    public Optional<ManifestDefaultEntry> getManifestDefault(String category, AppManifest.ManifestType manifestType) {
        List<ManifestDefaultEntry> manifests = getDefaultManifestsForCategory(category);
        return manifests.stream()
                .filter(m -> m.manifestType == manifestType)
                .findFirst();
    }

    /**
     * Check if a manifest should be created based on conditions
     */
    public boolean shouldCreateManifest(ManifestDefaultEntry manifest, JsonNode appConfig) {
        if (manifest.creationCondition == null || manifest.creationCondition.isEmpty()) {
            return manifest.required;
        }

        return evaluateManifestCondition(manifest.creationCondition, appConfig);
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
     * Get default configuration for auth based on app category and auth type from database
     */
    public JsonNode getDefaultAuthConfig(String category, String authType) {
        // Check cache first
        String cacheKey = category.toLowerCase() + "_" + authType.toLowerCase();
        if (authCache.containsKey(cacheKey)) {
            List<AuthDefault> cached = authCache.get(cacheKey);
            if (!cached.isEmpty()) {
                return cached.get(0).getDefaultConfig();
            }
        }
        
        // Load from database
        Optional<AuthDefault> authDefault = repository.findAuthDefault(category.toLowerCase(), authType.toLowerCase());
        
        if (authDefault.isPresent()) {
            // Cache for future use
            authCache.put(cacheKey, List.of(authDefault.get()));
            return authDefault.get().getDefaultConfig();
        }
        
        // Fallback to empty auth config
        ObjectNode fallbackConfig = objectMapper.createObjectNode();
        fallbackConfig.put("enabled", true);
        fallbackConfig.put("type", authType);
        return fallbackConfig;
    }

    /**
     * Get all available auth types for a category from database
     */
    public List<String> getAuthTypesForCategory(String category) {
        return repository.getAuthTypesForCategory(category.toLowerCase());
    }

    /**
     * Get all available service categories from database
     */
    public List<ServiceCategory> getAllServiceCategories() {
        return repository.findAllActiveCategories();
    }

    /**
     * Clear manifest cache (useful after database updates)
     */
    public void clearCache() {
        manifestCache.clear();
        authCache.clear();
    }

    /**
     * Check if category exists in database
     */
    public boolean categoryExists(String categoryName) {
        return repository.categoryExists(categoryName);
    }
} 