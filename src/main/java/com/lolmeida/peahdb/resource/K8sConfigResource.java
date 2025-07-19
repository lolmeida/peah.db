package com.lolmeida.peahdb.resource;


import com.fasterxml.jackson.databind.JsonNode;
import com.lolmeida.peahdb.entity.core.Environment;
import com.lolmeida.peahdb.entity.core.Stack;
import com.lolmeida.peahdb.entity.k8s.App;
import com.lolmeida.peahdb.entity.k8s.AppManifest;
import com.lolmeida.peahdb.entity.k8s.Deployment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/config")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class K8sConfigResource {

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
                envId, stackName, appName, AppManifest.ManifestType.valueOf(manifestType.toUpperCase())
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
