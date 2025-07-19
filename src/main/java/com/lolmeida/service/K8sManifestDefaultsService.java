package com.lolmeida.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lolmeida.entity.k8s.App;
import com.lolmeida.entity.k8s.AppManifest;
import com.lolmeida.entity.k8s.AuthDefault;
import com.lolmeida.entity.k8s.ManifestDefault;
import com.lolmeida.entity.k8s.ServiceCategory;
import com.lolmeida.repository.ManifestDefaultsRepository;
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

    // REMOVED: initializeDefaults() method - now using database-driven configuration

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
        List<ManifestDefaultEntry> defaults = getDefaultManifestsForCategory(app.category);
        List<AppManifest> manifests = new ArrayList<>();
        
        for (ManifestDefaultEntry defaultManifest : defaults) {
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