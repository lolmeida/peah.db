package com.lolmeida.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

// Entity imports
import com.lolmeida.dto.request.AppManifestRequest;
import com.lolmeida.dto.request.AppRequest;
import com.lolmeida.dto.request.EnvironmentRequest;
import com.lolmeida.dto.request.StackRequest;
import com.lolmeida.dto.response.*;
import com.lolmeida.entity.core.Environment;
import com.lolmeida.entity.core.Stack;
import com.lolmeida.entity.k8s.App;
import com.lolmeida.entity.k8s.AppManifest;
import com.lolmeida.entity.k8s.Deployment;
import com.lolmeida.entity.k8s.ServiceCategory;
import com.lolmeida.entity.k8s.*;

// DTO imports
import com.lolmeida.dto.request.*;
import com.lolmeida.dto.response.*;
import com.lolmeida.dto.mapper.MapperService;

// Service imports
import com.lolmeida.service.K8sService;
import com.lolmeida.service.DeploymentService;
import com.lolmeida.service.K8sManifestDefaultsService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Path("/api/config")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Configuration", description = "Kubernetes configuration management API")
public class K8sConfigResource {

    @Inject
    K8sService valuesGenerator;

    @Inject
    MapperService mapperService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    K8sManifestDefaultsService manifestDefaultsService;

    private ObjectMapper yamlMapper;

    @jakarta.annotation.PostConstruct
    void init() {
        yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    // ENVIRONMENTS
    @GET
    @Path("/environments")
    @Operation(summary = "Get all environments", description = "Retrieve all available environments")
    @APIResponse(responseCode = "200", description = "List of environments")
    public List<EnvironmentResponse> getEnvironments() {
        List<Environment> environments = Environment.listAll();
        return environments.stream()
                .map(mapperService::toEnvironmentResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/environments/{envId}")
    public Response getEnvironment(@PathParam("envId") Long envId) {
        Environment environment = Environment.findById(envId);
        if (environment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapperService.toEnvironmentResponse(environment)).build();
    }

    @POST
    @Path("/environments")
    @Transactional
    public Response createEnvironment(EnvironmentRequest environmentRequest) {
        Environment environment = mapperService.toEnvironment(environmentRequest);
        environment.persist();
        return Response.status(Response.Status.CREATED)
                .entity(mapperService.toEnvironmentResponse(environment))
                .build();
    }

    @PUT
    @Path("/environments/{envId}")
    @Transactional
    public Response updateEnvironment(@PathParam("envId") Long envId, EnvironmentRequest environmentRequest) {
        Environment existingEnvironment = Environment.findById(envId);
        if (existingEnvironment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Environment updatedEnvironment = mapperService.toEnvironmentWithId(environmentRequest, envId);
        existingEnvironment.name = updatedEnvironment.name;
        existingEnvironment.description = updatedEnvironment.description;
        existingEnvironment.isActive = updatedEnvironment.isActive;
        
        return Response.ok(mapperService.toEnvironmentResponse(existingEnvironment)).build();
    }

    // STACKS
    @GET
    @Path("/environments/{envId}/stacks")
    public List<StackResponse> getStacks(@PathParam("envId") Long envId) {
        List<Stack> stacks = Stack.find("environment.id", envId).list();
        return stacks.stream()
                .map(mapperService::toStackResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/environments/{envId}/stacks/{stackId}")
    public Response getStack(@PathParam("envId") Long envId, @PathParam("stackId") Long stackId) {
        Stack stack = Stack.find("id = ?1 and environment.id = ?2", stackId, envId).firstResult();
        if (stack == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapperService.toStackResponse(stack)).build();
    }

    @POST
    @Path("/environments/{envId}/stacks")
    @Transactional
    public Response createStack(@PathParam("envId") Long envId, StackRequest stackRequest) {
        // Verify environment exists
        Environment environment = Environment.findById(envId);
        if (environment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Environment not found")
                    .build();
        }
        
        // Ensure the stackRequest has the correct environmentId
        stackRequest.setEnvironmentId(envId);
        Stack stack = mapperService.toStack(stackRequest);
        stack.persist();
        
        return Response.status(Response.Status.CREATED)
                .entity(mapperService.toStackResponse(stack))
                .build();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackId}")
    @Transactional
    public Response updateStack(@PathParam("envId") Long envId,
                                @PathParam("stackId") Long stackId,
                                StackRequest stackRequest) {
        Stack existingStack = Stack.find("id = ?1 and environment.id = ?2", stackId, envId).firstResult();
        if (existingStack == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure the stackRequest has the correct environmentId
        stackRequest.setEnvironmentId(envId);
        Stack updatedStack = mapperService.toStackWithId(stackRequest, stackId);
        
        existingStack.name = updatedStack.name;
        existingStack.enabled = updatedStack.enabled;
        existingStack.description = updatedStack.description;
        existingStack.config = updatedStack.config;
        
        return Response.ok(mapperService.toStackResponse(existingStack)).build();
    }

    // UPDATE STACK by name (frontend compatibility)
    @PUT
    @Path("/environments/{envId}/stacks/{stackName}")
    @Transactional
    public Response updateStackByName(@PathParam("envId") Long envId,
                                     @PathParam("stackName") String stackName,
                                     StackRequest stackRequest) {
        Stack existingStack = Stack.find("environment.id = ?1 and name = ?2", envId, stackName).firstResult();
        if (existingStack == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure the stackRequest has the correct environmentId
        stackRequest.setEnvironmentId(envId);
        Stack updatedStack = mapperService.toStackWithId(stackRequest, existingStack.id);
        
        existingStack.name = updatedStack.name;
        existingStack.enabled = updatedStack.enabled;
        existingStack.description = updatedStack.description;
        existingStack.config = updatedStack.config;
        
        return Response.ok(mapperService.toStackResponse(existingStack)).build();
    }

    // APPS
    @GET
    @Path("/environments/{envId}/stacks/{stackId}/apps")
    public List<AppResponse> getApps(@PathParam("envId") Long envId,
                                     @PathParam("stackId") Long stackId) {
        List<App> apps = App.find(
                "stack.environment.id = ?1 and stack.id = ?2 ORDER BY deploymentPriority, name",
                envId, stackId
        ).list();
        return apps.stream()
                .map(mapperService::toAppResponse)
                .collect(Collectors.toList());
    }

    // APPS by stack name (frontend compatibility)
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/apps")
    public List<AppResponse> getAppsByStackName(@PathParam("envId") Long envId,
                                               @PathParam("stackName") String stackName) {
        List<App> apps = App.find(
                "stack.environment.id = ?1 and stack.name = ?2 ORDER BY deploymentPriority, name",
                envId, stackName
        ).list();
        return apps.stream()
                .map(mapperService::toAppResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}")
    public Response getApp(@PathParam("envId") Long envId,
                           @PathParam("stackId") Long stackId,
                           @PathParam("appId") Long appId) {
        App app = App.find(
                "id = ?1 and stack.environment.id = ?2 and stack.id = ?3",
                appId, envId, stackId
        ).firstResult();
        if (app == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapperService.toAppResponse(app)).build();
    }

    @POST
    @Path("/environments/{envId}/stacks/{stackId}/apps")
    @Transactional
    public Response createApp(@PathParam("envId") Long envId,
                              @PathParam("stackId") Long stackId,
                              AppRequest appRequest) {
        // Verify stack exists
        Stack stack = Stack.find("id = ?1 and environment.id = ?2", stackId, envId).firstResult();
        if (stack == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Stack not found")
                    .build();
        }
        
        // Ensure the appRequest has the correct stackId
        appRequest.setStackId(stackId);
        App app = mapperService.toApp(appRequest);
        app.persist();
        
        return Response.status(Response.Status.CREATED)
                .entity(mapperService.toAppResponse(app))
                .build();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}")
    @Transactional
    public Response updateApp(@PathParam("envId") Long envId,
                              @PathParam("stackId") Long stackId,
                              @PathParam("appId") Long appId,
                              AppRequest appRequest) {
        App existingApp = App.find(
                "id = ?1 and stack.environment.id = ?2 and stack.id = ?3",
                appId, envId, stackId
        ).firstResult();

        if (existingApp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure the appRequest has the correct stackId
        appRequest.setStackId(stackId);
        App updatedApp = mapperService.toAppWithId(appRequest, appId);
        
        existingApp.name = updatedApp.name;
        existingApp.enabled = updatedApp.enabled;
        existingApp.defaultConfig = updatedApp.defaultConfig;
        existingApp.deploymentPriority = updatedApp.deploymentPriority;
        existingApp.displayName = updatedApp.displayName;
        existingApp.description = updatedApp.description;
        existingApp.category = updatedApp.category;
        existingApp.version = updatedApp.version;
        existingApp.defaultImageRepository = updatedApp.defaultImageRepository;
        existingApp.defaultImageTag = updatedApp.defaultImageTag;
        
        return Response.ok(mapperService.toAppResponse(existingApp)).build();
    }

    // UPDATE APP by stack name and app name (frontend compatibility)
    @PUT
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}")
    @Transactional
    public Response updateAppByName(@PathParam("envId") Long envId,
                                   @PathParam("stackName") String stackName,
                                   @PathParam("appName") String appName,
                                   AppRequest appRequest) {
        App existingApp = App.find(
                "stack.environment.id = ?1 and stack.name = ?2 and name = ?3",
                envId, stackName, appName
        ).firstResult();

        if (existingApp == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure the appRequest has the correct stackId
        appRequest.setStackId(existingApp.stack.id);
        App updatedApp = mapperService.toAppWithId(appRequest, existingApp.id);
        
        existingApp.name = updatedApp.name;
        existingApp.enabled = updatedApp.enabled;
        existingApp.defaultConfig = updatedApp.defaultConfig;
        existingApp.deploymentPriority = updatedApp.deploymentPriority;
        existingApp.displayName = updatedApp.displayName;
        existingApp.description = updatedApp.description;
        existingApp.category = updatedApp.category;
        existingApp.version = updatedApp.version;
        existingApp.defaultImageRepository = updatedApp.defaultImageRepository;
        existingApp.defaultImageTag = updatedApp.defaultImageTag;
        
        return Response.ok(mapperService.toAppResponse(existingApp)).build();
    }

    // APP MANIFESTS
    @GET
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}/manifests")
    public List<AppManifestResponse> getAppManifests(@PathParam("envId") Long envId,
                                                     @PathParam("stackId") Long stackId,
                                                     @PathParam("appId") Long appId) {
        List<AppManifest> manifests = AppManifest.find(
                "app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3 ORDER BY creationPriority",
                appId, envId, stackId
        ).list();
        return manifests.stream()
                .map(mapperService::toAppManifestResponse)
                .collect(Collectors.toList());
    }

    // APP MANIFESTS by stack name and app name (frontend compatibility)
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/apps/{appName}/manifests")
    public List<AppManifestResponse> getAppManifestsByName(@PathParam("envId") Long envId,
                                                          @PathParam("stackName") String stackName,
                                                          @PathParam("appName") String appName) {
        List<AppManifest> manifests = AppManifest.find(
                "app.stack.environment.id = ?1 and app.stack.name = ?2 and app.name = ?3 ORDER BY creationPriority",
                envId, stackName, appName
        ).list();
        return manifests.stream()
                .map(mapperService::toAppManifestResponse)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}/manifests/{manifestId}")
    public Response getAppManifest(@PathParam("envId") Long envId,
                                   @PathParam("stackId") Long stackId,
                                   @PathParam("appId") Long appId,
                                   @PathParam("manifestId") Long manifestId) {
        AppManifest manifest = AppManifest.find(
                "id = ?1 and app.id = ?2 and app.stack.environment.id = ?3 and app.stack.id = ?4",
                manifestId, appId, envId, stackId
        ).firstResult();
        if (manifest == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapperService.toAppManifestResponse(manifest)).build();
    }

    @POST
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}/manifests")
    @Transactional
    public Response createAppManifest(@PathParam("envId") Long envId,
                                      @PathParam("stackId") Long stackId,
                                      @PathParam("appId") Long appId,
                                      AppManifestRequest appManifestRequest) {
        // Verify app exists
        App app = App.find("id = ?1 and stack.environment.id = ?2 and stack.id = ?3", appId, envId, stackId).firstResult();
        if (app == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("App not found")
                    .build();
        }
        
        // Ensure the request has the correct appId
        appManifestRequest.setAppId(appId);
        AppManifest manifest = mapperService.toAppManifest(appManifestRequest);
        manifest.persist();
        
        return Response.status(Response.Status.CREATED)
                .entity(mapperService.toAppManifestResponse(manifest))
                .build();
    }

    @PUT
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}/manifests/{manifestId}")
    @Transactional
    public Response updateAppManifest(@PathParam("envId") Long envId,
                                      @PathParam("stackId") Long stackId,
                                      @PathParam("appId") Long appId,
                                      @PathParam("manifestId") Long manifestId,
                                      AppManifestRequest appManifestRequest) {
        AppManifest existingManifest = AppManifest.find(
                "id = ?1 and app.id = ?2 and app.stack.environment.id = ?3 and app.stack.id = ?4",
                manifestId, appId, envId, stackId
        ).firstResult();

        if (existingManifest == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure the request has the correct appId
        appManifestRequest.setAppId(appId);
        AppManifest updatedManifest = mapperService.toAppManifestWithId(appManifestRequest, manifestId);
        
        existingManifest.manifestType = updatedManifest.manifestType;
        existingManifest.required = updatedManifest.required;
        existingManifest.defaultConfig = updatedManifest.defaultConfig;
        existingManifest.creationCondition = updatedManifest.creationCondition;
        existingManifest.creationPriority = updatedManifest.creationPriority;
        
        return Response.ok(mapperService.toAppManifestResponse(existingManifest)).build();
    }

    // KUBERNETES RESOURCES (Direct Access)
    @GET
    @Path("/environments/{envId}/stacks/{stackId}/apps/{appId}/deployments")
    public List<DeploymentResponse> getDeployments(@PathParam("envId") Long envId,
                                                   @PathParam("stackId") Long stackId,
                                                   @PathParam("appId") Long appId) {
        List<Deployment> deployments = Deployment.find(
                "app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3",
                appId, envId, stackId
        ).list();
        return deployments.stream()
                .map(mapperService::toDeploymentResponse)
                .collect(Collectors.toList());
    }

    // TODO: Add other K8s resources endpoints (Secret, ConfigMap, Service, etc.) as needed

    // VALUES GENERATION
    @GET
    @Path("/environments/{envId}/stacks/{stackName}/values")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateValues(@PathParam("envId") Long envId,
                                   @PathParam("stackName") String stackName) {
        return valuesGenerator.generateStackValues(envId, stackName);
    }

    @GET
    @Path("/environments/{envId}/stacks/{stackName}/values.yaml")
    @Produces("application/x-yaml")
    public Response generateValuesYaml(@PathParam("envId") Long envId,
                                       @PathParam("stackName") String stackName) {
        Response response = valuesGenerator.generateStackValues(envId, stackName);
        
        // If the service returned an error, propagate it
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            return response;
        }
        
        JsonNode values = (JsonNode) response.getEntity();
        try {
            String yamlContent = yamlMapper.writeValueAsString(values);
            return Response.ok(yamlContent).type("application/x-yaml").build();
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to generate YAML values: " + e.getMessage())
                    .build();
        }
    }

    @Inject
    DeploymentService deploymentService;

    // DEPLOY STACK - Environment-specific deployment strategies
    @POST
    @Path("/environments/{envId}/stacks/{stackName}/deploy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deployStack(@PathParam("envId") Long envId,
                               @PathParam("stackName") String stackName) {
        Environment env = Environment.findById(envId);
        if (env == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Environment not found\"}")
                    .build();
        }

        Stack stack = Stack.find("environment.id = ?1 and name = ?2", envId, stackName).firstResult();
        if (stack == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Stack not found\"}")
                    .build();
        }

        if (!stack.enabled) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Stack is not enabled\"}")
                    .build();
        }

        // Execute environment-specific deployment strategy
        DeploymentService.DeploymentResult result = deploymentService.deployStack(env, stack);
        
        if (result.success) {
            return Response.ok(result.toJson()).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(result.toJson())
                    .build();
        }
    }

    // ========== MANIFEST DEFAULTS ENDPOINTS ==========

    @GET
    @Path("/manifests/defaults/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get default manifests for category", description = "Get the default Kubernetes manifests for a service category")
    @APIResponse(responseCode = "200", description = "Default manifests retrieved successfully")
    public Response getManifestDefaults(@PathParam("category") String category) {
        List<K8sManifestDefaultsService.ManifestDefaultEntry> defaults = 
            manifestDefaultsService.getDefaultManifestsForCategory(category);
        
        // Convert to a more REST-friendly format
        List<Map<String, Object>> manifestsInfo = defaults.stream()
            .map(manifest -> {
                Map<String, Object> info = new HashMap<>();
                info.put("manifestType", manifest.manifestType.name());
                info.put("required", manifest.required);
                info.put("creationPriority", manifest.creationPriority);
                info.put("description", manifest.description);
                info.put("creationCondition", manifest.creationCondition);
                info.put("defaultConfig", manifest.defaultConfig);
                return info;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("manifests", manifestsInfo);
        response.put("totalCount", manifestsInfo.size());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/manifests/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get available manifest categories", description = "Get list of available service categories with manifest support from database")
    @APIResponse(responseCode = "200", description = "Categories retrieved successfully")
    public Response getManifestCategories() {
        List<ServiceCategory> categories = manifestDefaultsService.getAllServiceCategories();
        
        // Convert to response format
        List<Map<String, Object>> categoryList = categories.stream()
            .map(cat -> {
                Map<String, Object> info = new HashMap<>();
                info.put("name", cat.getName());
                info.put("displayName", cat.getDisplayName());
                info.put("description", cat.getDescription());
                info.put("icon", cat.getIcon());
                info.put("color", cat.getColor());
                info.put("active", cat.getIsActive());
                return info;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("categories", categoryList);
        response.put("totalCount", categoryList.size());
        response.put("description", "Available service categories loaded from database");
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/manifests/types")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all manifest types", description = "Get all available Kubernetes manifest types")
    @APIResponse(responseCode = "200", description = "Manifest types retrieved successfully")  
    public Response getAllManifestTypes() {
        Set<String> types = manifestDefaultsService.getAllManifestTypes();
        
        Map<String, Object> response = new HashMap<>();
        response.put("manifestTypes", types);
        response.put("totalCount", types.size());
        
        return Response.ok(response).build();
    }

    // ========== AUTH DEFAULTS ENDPOINTS ==========

    @GET
    @Path("/auth/defaults/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get auth defaults for category", description = "Get available authentication configurations for a service category")
    @APIResponse(responseCode = "200", description = "Auth defaults retrieved successfully")
    public Response getAuthDefaults(@PathParam("category") String category) {
        List<String> authTypes = manifestDefaultsService.getAuthTypesForCategory(category);
        
        // Get details for each auth type
        List<Map<String, Object>> authDetails = authTypes.stream()
            .map(authType -> {
                Map<String, Object> details = new HashMap<>();
                details.put("authType", authType);
                details.put("defaultConfig", manifestDefaultsService.getDefaultAuthConfig(category, authType));
                return details;
            })
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("authTypes", authDetails);
        response.put("totalCount", authDetails.size());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/auth/defaults/{category}/{authType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get specific auth config", description = "Get default authentication configuration for a specific category and auth type")
    @APIResponse(responseCode = "200", description = "Auth config retrieved successfully")
    @APIResponse(responseCode = "404", description = "Auth config not found")
    public Response getAuthConfig(@PathParam("category") String category, @PathParam("authType") String authType) {
        JsonNode authConfig = manifestDefaultsService.getDefaultAuthConfig(category, authType);
        
        if (authConfig == null || authConfig.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Auth configuration not found for category: " + category + ", type: " + authType)
                    .build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("authType", authType);
        response.put("defaultConfig", authConfig);
        
        return Response.ok(response).build();
    }

    // ========== CACHE MANAGEMENT ENDPOINTS ==========

    @POST
    @Path("/cache/clear")
    @Operation(summary = "Clear manifest cache", description = "Clear the manifest and auth defaults cache to force reload from database")
    @APIResponse(responseCode = "200", description = "Cache cleared successfully")
    public Response clearCache() {
        manifestDefaultsService.clearCache();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manifest and auth defaults cache cleared successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Response.ok(response).build();
    }

    @GET
    @Path("/cache/stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get cache statistics", description = "Get information about cached manifest and auth defaults")
    @APIResponse(responseCode = "200", description = "Cache stats retrieved successfully")
    public Response getCacheStats() {
        // This would be implemented to show cache hit rates, size, etc.
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Cache statistics endpoint - to be implemented");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return Response.ok(response).build();
    }
}
