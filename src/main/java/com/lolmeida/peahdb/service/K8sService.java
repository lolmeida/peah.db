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
        // Get all required manifests for this app
        List<AppManifest> manifests = repository.findAppManifestsByAppId(app.id);

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

    private void addManifestDefaults(AppManifest.ManifestType manifestType, ObjectNode config, App app) {
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
                config.put("size", "2Gi");
                break;
            case HPA:
                config.put("minReplicas", 1);
                config.put("maxReplicas", 2);
                config.put("targetCPUUtilizationPercentage", 70);
                break;
        }
    }
}