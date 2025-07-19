package com.lolmeida.peahdb.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lolmeida.peahdb.entity.core.Environment;
import com.lolmeida.peahdb.entity.core.Stack;
import com.lolmeida.peahdb.entity.k8s.App;
import com.lolmeida.peahdb.entity.k8s.AppManifest;
import com.lolmeida.peahdb.repository.K8sRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.List;

@ApplicationScoped
public class K8sService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    K8sRepository repository;

    @Inject
    K8sManifestDefaultsService manifestDefaultsService;

    public Response generateStackValues(Long envId, String stackName) {
        if (envId == null || stackName == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Environment ID and Stack name cannot be null");
        }

        ObjectNode values = objectMapper.createObjectNode();

        // Global configuration
        Environment env = repository.findEnvironmentById(envId).orElse(null);
        if (env == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Environment with id " + envId + " not found");
        }
        
        ObjectNode global = values.putObject("global");
        global.put("namespace", env.name.equals("prod") ? "lolmeida" : env.name);
        global.put("timezone", "Europe/Lisbon");

        // Stack configuration
        Stack stack = repository.findStackByEnvironmentAndName(envId, stackName).orElse(null);
        if (stack == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Stack '" + stackName + "' not found in environment " + envId);
        }

        // Stack-level flags
        ObjectNode stackConfig = values.putObject(stackName + "Stack");
        stackConfig.put("enabled", stack.enabled);

        ObjectNode stackApps = stackConfig.putObject("apps");

        // Apps configuration (sorted by deployment priority)
        List<App> apps = repository.findAppsByStackId(stack.id);
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

        return BaseService.result(Response.Status.OK, values);
    }
    
    // ========== ENVIRONMENT OPERATIONS ==========
    
    public Response getAllEnvironments() {
        return BaseService.result(Response.Status.OK, repository.findAllEnvironments());
    }
    
    public Response getEnvironmentById(Long envId) {
        if (envId == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Environment ID cannot be null");
        }
        
        Environment env = repository.findEnvironmentById(envId).orElse(null);
        if (env == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Environment with id " + envId + " not found");
        }
        
        return BaseService.result(Response.Status.OK, env);
    }
    
    // ========== STACK OPERATIONS ==========
    
    public Response getStacksByEnvironmentId(Long envId) {
        if (envId == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Environment ID cannot be null");
        }
        
        // Verify environment exists
        Environment env = repository.findEnvironmentById(envId).orElse(null);
        if (env == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Environment with id " + envId + " not found");
        }
        
        return BaseService.result(Response.Status.OK, repository.findStacksByEnvironmentId(envId));
    }
    
    public Response getStackByEnvironmentAndName(Long envId, String stackName) {
        if (envId == null || stackName == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Environment ID and Stack name cannot be null");
        }
        
        Stack stack = repository.findStackByEnvironmentAndName(envId, stackName).orElse(null);
        if (stack == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Stack '" + stackName + "' not found in environment " + envId);
        }
        
        return BaseService.result(Response.Status.OK, stack);
    }
    
    // ========== APP OPERATIONS ==========
    
    public Response getAppsByStackId(Long stackId) {
        if (stackId == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Stack ID cannot be null");
        }
        
        return BaseService.result(Response.Status.OK, repository.findAppsByStackId(stackId));
    }
    
    public Response getAppsByEnvironmentAndStack(Long envId, Long stackId) {
        if (envId == null || stackId == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "Environment ID and Stack ID cannot be null");
        }
        
        // Verify stack exists in environment
        Stack stack = repository.findStackByIdAndEnvironment(stackId, envId).orElse(null);
        if (stack == null) {
            return BaseService.result(Response.Status.NOT_FOUND, "Stack with id " + stackId + " not found in environment " + envId);
        }
        
        return BaseService.result(Response.Status.OK, repository.findAppsByEnvironmentAndStack(envId, stackId));
    }
    
    // ========== APP MANIFEST OPERATIONS ==========
    
    public Response getAppManifestsByAppId(Long appId) {
        if (appId == null) {
            return BaseService.result(Response.Status.BAD_REQUEST, "App ID cannot be null");
        }
        
        return BaseService.result(Response.Status.OK, repository.findAppManifestsByAppId(appId));
    }

    private void generateAppManifestConfigurations(App app, ObjectNode appConfig) {
        // Get default manifests for this app category
        List<K8sManifestDefaultsService.ManifestDefault> defaultManifests = 
            manifestDefaultsService.getDefaultManifestsForCategory(app.category);
        
        // Get existing manifests for this app (if any)
        List<AppManifest> existingManifests = repository.findAppManifestsByAppId(app.id);
        
        // Generate configurations for default manifests
        for (K8sManifestDefaultsService.ManifestDefault defaultManifest : defaultManifests) {
            if (manifestDefaultsService.evaluateManifestCondition(defaultManifest.creationCondition, appConfig)) {
                String manifestKey = defaultManifest.manifestType.name().toLowerCase();
                
                ObjectNode manifestConfig = appConfig.putObject(manifestKey);
                manifestConfig.put("enabled", true);
                
                // Add default configuration from the manifest defaults service
                if (!defaultManifest.defaultConfig.isEmpty()) {
                    ObjectNode defaultConfigNode = objectMapper.valueToTree(defaultManifest.defaultConfig);
                    manifestConfig.setAll(defaultConfigNode);
                }
                
                // Add legacy manifest-specific defaults for backwards compatibility
                addManifestDefaults(defaultManifest.manifestType, manifestConfig, app);
            }
        }
        
        // Process existing manifests (for apps that have custom manifests defined)
        for (AppManifest manifest : existingManifests) {
            String manifestKey = manifest.getManifestTypeName().toLowerCase();

            // Only generate config for required manifests or those with conditions met
            if (manifest.isRequired() || shouldCreateManifest(manifest, appConfig)) {
                ObjectNode manifestConfig = appConfig.hasNonNull(manifestKey) ? 
                    (ObjectNode) appConfig.get(manifestKey) : appConfig.putObject(manifestKey);
                
                manifestConfig.put("enabled", true);

                // Add custom manifest configuration if available
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

        // Use the enhanced condition evaluation from manifest defaults service
        return manifestDefaultsService.evaluateManifestCondition(manifest.creationCondition, appConfig);
    }

    private void addManifestDefaults(AppManifest.ManifestType manifestType, ObjectNode config, App app) {
        switch (manifestType) {
            case DEPLOYMENT:
                // Enhanced deployment configuration based on app category
                if (!config.has("replicaCount")) {
                    int defaultReplicas = "api".equals(app.category) ? 2 : 1;
                    config.put("replicaCount", defaultReplicas);
                }
                if (!config.has("imagePullPolicy")) {
                    config.put("imagePullPolicy", "IfNotPresent");
                }
                if (!config.has("restartPolicy")) {
                    config.put("restartPolicy", "Always");
                }
                
                // Add rolling update strategy for APIs
                if ("api".equals(app.category) && !config.has("strategy")) {
                    ObjectNode strategy = config.putObject("strategy");
                    strategy.put("type", "RollingUpdate");
                    ObjectNode rollingUpdate = strategy.putObject("rollingUpdate");
                    rollingUpdate.put("maxSurge", 1);
                    rollingUpdate.put("maxUnavailable", 0);
                }
                break;
                
            case SERVICE:
                if (!config.has("type")) {
                    config.put("type", "ClusterIP");
                }
                if (!config.has("sessionAffinity")) {
                    config.put("sessionAffinity", "None");
                }
                break;
                
            case INGRESS:
                if (!config.has("className")) {
                    config.put("className", "nginx");
                }
                if (!config.has("host")) {
                    config.put("host", app.name + ".lolmeida.com");
                }
                
                // Enhanced annotations based on app category
                ObjectNode annotations = config.has("annotations") ? 
                    (ObjectNode) config.get("annotations") : config.putObject("annotations");
                
                annotations.put("nginx.ingress.kubernetes.io/ssl-redirect", "true");
                annotations.put("cert-manager.io/cluster-issuer", "letsencrypt-prod");
                
                // Add rate limiting for APIs
                if ("api".equals(app.category)) {
                    annotations.put("nginx.ingress.kubernetes.io/rate-limit", "100");
                    annotations.put("nginx.ingress.kubernetes.io/rate-limit-window", "1m");
                }
                
                // Add TLS configuration
                if (!config.has("tls")) {
                    ObjectNode tls = config.putObject("tls");
                    tls.put("enabled", true);
                }
                break;
                
            case PERSISTENT_VOLUME_CLAIM:
                if (!config.has("accessMode")) {
                    config.put("accessMode", "ReadWriteOnce");
                }
                if (!config.has("size")) {
                    // Different default sizes based on app category
                    String defaultSize = switch (app.category) {
                        case "database" -> "10Gi";
                        case "monitoring" -> "20Gi";
                        case "automation" -> "5Gi";
                        default -> "2Gi";
                    };
                    config.put("size", defaultSize);
                }
                if (!config.has("storageClass")) {
                    config.put("storageClass", "default");
                }
                break;
                
            case HPA:
                if (!config.has("minReplicas")) {
                    int minReplicas = "api".equals(app.category) ? 2 : 1;
                    config.put("minReplicas", minReplicas);
                }
                if (!config.has("maxReplicas")) {
                    int maxReplicas = "api".equals(app.category) ? 10 : 3;
                    config.put("maxReplicas", maxReplicas);
                }
                if (!config.has("targetCPUUtilizationPercentage")) {
                    config.put("targetCPUUtilizationPercentage", 70);
                }
                if ("api".equals(app.category) && !config.has("targetMemoryUtilizationPercentage")) {
                    config.put("targetMemoryUtilizationPercentage", 80);
                }
                break;
                
            case SECRET:
                if (!config.has("type")) {
                    config.put("type", "Opaque");
                }
                if (!config.has("immutable")) {
                    config.put("immutable", false);
                }
                break;
                
            case CONFIG_MAP:
                if (!config.has("immutable")) {
                    config.put("immutable", false);
                }
                break;
                
            case SERVICE_ACCOUNT:
                if (!config.has("automountServiceAccountToken")) {
                    config.put("automountServiceAccountToken", true);
                }
                break;
                
            case CLUSTER_ROLE:
                if (!config.has("createBinding")) {
                    config.put("createBinding", true);
                }
                break;
        }
    }
}