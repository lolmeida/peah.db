package com.lolmeida.peahdb.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lolmeida.peahdb.entity.core.Environment;
import com.lolmeida.peahdb.entity.core.Stack;
import com.lolmeida.peahdb.entity.k8s.App;
import com.lolmeida.peahdb.entity.k8s.AppManifest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class K8sValuesGeneratorService {

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