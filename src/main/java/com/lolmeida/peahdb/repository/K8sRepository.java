package com.lolmeida.peahdb.repository;

import com.lolmeida.peahdb.entity.core.Environment;
import com.lolmeida.peahdb.entity.core.Stack;
import com.lolmeida.peahdb.entity.k8s.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class K8sRepository {

    // ========== ENVIRONMENT OPERATIONS ==========
    
    public List<Environment> findAllEnvironments() {
        return Environment.listAll();
    }
    
    public Optional<Environment> findEnvironmentById(Long envId) {
        return Optional.ofNullable(Environment.findById(envId));
    }
    
    public Environment persistEnvironment(Environment environment) {
        environment.persist();
        return environment;
    }

    // ========== STACK OPERATIONS ==========
    
    public List<Stack> findStacksByEnvironmentId(Long envId) {
        return Stack.find("environment.id", envId).list();
    }
    
    public Optional<Stack> findStackByEnvironmentAndName(Long envId, String stackName) {
        return Stack.find("environment.id = ?1 and name = ?2", envId, stackName).firstResultOptional();
    }
    
    public Optional<Stack> findStackByIdAndEnvironment(Long stackId, Long envId) {
        return Stack.find("id = ?1 and environment.id = ?2", stackId, envId).firstResultOptional();
    }
    
    public Stack persistStack(Stack stack) {
        stack.persist();
        return stack;
    }

    // ========== APP OPERATIONS ==========
    
    public List<App> findAppsByStackId(Long stackId) {
        return App.find("stack.id = ?1 ORDER BY deploymentPriority, name", stackId).list();
    }
    
    public List<App> findAppsByEnvironmentAndStack(Long envId, Long stackId) {
        return App.find("stack.environment.id = ?1 and stack.id = ?2 ORDER BY deploymentPriority, name", 
                       envId, stackId).list();
    }
    
    public Optional<App> findAppByIdAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return App.find("id = ?1 and stack.environment.id = ?2 and stack.id = ?3", 
                       appId, envId, stackId).firstResultOptional();
    }
    
    public App persistApp(App app) {
        app.persist();
        return app;
    }

    // ========== APP MANIFEST OPERATIONS ==========
    
    public List<AppManifest> findAppManifestsByAppId(Long appId) {
        return AppManifest.find("app.id = ?1 ORDER BY creationPriority", appId).list();
    }
    
    public List<AppManifest> findAppManifestsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return AppManifest.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3 ORDER BY creationPriority", 
                               appId, envId, stackId).list();
    }
    
    public Optional<AppManifest> findAppManifestById(Long manifestId, Long appId, Long envId, Long stackId) {
        return AppManifest.find("id = ?1 and app.id = ?2 and app.stack.environment.id = ?3 and app.stack.id = ?4", 
                               manifestId, appId, envId, stackId).firstResultOptional();
    }
    
    public AppManifest persistAppManifest(AppManifest manifest) {
        manifest.persist();
        return manifest;
    }

    // ========== DEPLOYMENT OPERATIONS ==========
    
    public List<Deployment> findDeploymentsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return Deployment.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                              appId, envId, stackId).list();
    }
    
    public Deployment persistDeployment(Deployment deployment) {
        deployment.persist();
        return deployment;
    }

    // ========== SECRET OPERATIONS ==========
    
    public List<Secret> findSecretsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return Secret.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                          appId, envId, stackId).list();
    }
    
    public Secret persistSecret(Secret secret) {
        secret.persist();
        return secret;
    }

    // ========== CONFIG MAP OPERATIONS ==========
    
    public List<ConfigMap> findConfigMapsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return ConfigMap.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                             appId, envId, stackId).list();
    }
    
    public ConfigMap persistConfigMap(ConfigMap configMap) {
        configMap.persist();
        return configMap;
    }

    // ========== SERVICE OPERATIONS ==========
    
    public List<K8sService> findServicesByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return K8sService.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                              appId, envId, stackId).list();
    }
    
    public K8sService persistService(K8sService service) {
        service.persist();
        return service;
    }

    // ========== INGRESS OPERATIONS ==========
    
    public List<Ingress> findIngressByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return Ingress.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                           appId, envId, stackId).list();
    }
    
    public Ingress persistIngress(Ingress ingress) {
        ingress.persist();
        return ingress;
    }

    // ========== PERSISTENT VOLUME CLAIM OPERATIONS ==========
    
    public List<PersistentVolumeClaim> findPVCsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return PersistentVolumeClaim.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                                         appId, envId, stackId).list();
    }
    
    public PersistentVolumeClaim persistPVC(PersistentVolumeClaim pvc) {
        pvc.persist();
        return pvc;
    }

    // ========== SERVICE ACCOUNT OPERATIONS ==========
    
    public List<ServiceAccount> findServiceAccountsByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return ServiceAccount.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                                  appId, envId, stackId).list();
    }
    
    public ServiceAccount persistServiceAccount(ServiceAccount serviceAccount) {
        serviceAccount.persist();
        return serviceAccount;
    }

    // ========== HPA OPERATIONS ==========
    
    public List<Hpa> findHpasByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return Hpa.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                       appId, envId, stackId).list();
    }
    
    public Hpa persistHpa(Hpa hpa) {
        hpa.persist();
        return hpa;
    }

    // ========== CLUSTER ROLE OPERATIONS ==========
    
    public List<ClusterRole> findClusterRolesByAppAndEnvironmentAndStack(Long appId, Long envId, Long stackId) {
        return ClusterRole.find("app.id = ?1 and app.stack.environment.id = ?2 and app.stack.id = ?3", 
                               appId, envId, stackId).list();
    }
    
    public ClusterRole persistClusterRole(ClusterRole clusterRole) {
        clusterRole.persist();
        return clusterRole;
    }
} 