package com.lolmeida.entity.k8s;

import com.lolmeida.entity.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AppManifest Entity
 * Defines which Kubernetes manifest types each app requires
 *  a Many-to-Many relationship between Apps and Manifest Types
 */
@Entity
@Table(name = "config_app_manifests",
       uniqueConstraints = @UniqueConstraint(columnNames = {"app_id", "manifest_type"}))
public class AppManifest extends BaseEntity {

    // Reference to app
    @ManyToOne
    @JoinColumn(name = "app_id")
    public App app;

    // Type of Kubernetes manifest
    @Column(name = "manifest_type")
    @Enumerated(EnumType.STRING)
    public ManifestType manifestType;

    // Is this manifest required or optional for the app
    @Column(name = "required")
    public Boolean required = true;

    // Priority for creation (lower = create first)
    @Column(name = "creation_priority")
    public Integer creationPriority = 100;

    // Default configuration specific to this app+manifest combination
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_config")
    public JsonNode defaultConfig;

    // Template overrides (if this app needs special template logic)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_overrides")
    public JsonNode templateOverrides;

    // Conditions when this manifest should be created
    @Column(name = "creation_condition")
    public String creationCondition; // e.g., "persistence.enabled", "ingress.enabled"

    // Description of why this manifest is needed
    @Column(name = "description")
    public String description;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    // Constructors
    public AppManifest() {
    }

    public AppManifest(App app, ManifestType manifestType, boolean required) {
        this.app = app;
        this.manifestType = manifestType;
        this.required = required;
    }

    public AppManifest(App app, ManifestType manifestType, boolean required, int priority) {
        this.app = app;
        this.manifestType = manifestType;
        this.required = required;
        this.creationPriority = priority;
    }

    // Helper methods
    public boolean isRequired() {
        return required != null && required;
    }

    public boolean isOptional() {
        return !isRequired();
    }

    public boolean hasCondition() {
        return creationCondition != null && !creationCondition.isEmpty();
    }

    public boolean hasDefaultConfig() {
        return defaultConfig != null && !defaultConfig.isEmpty();
    }

    public boolean hasTemplateOverrides() {
        return templateOverrides != null && !templateOverrides.isEmpty();
    }

    public boolean isHighPriority() {
        return creationPriority != null && creationPriority < 50;
    }

    public String getManifestTypeName() {
        return manifestType != null ? manifestType.name().toLowerCase() : "unknown";
    }

    public String getKubernetesKind() {
        return manifestType != null ? manifestType.kubernetesKind : null;
    }

    public String getTemplateFile() {
        return manifestType != null ? manifestType.templateFile : null;
    }

    public Class<?> getEntityClass() {
        return manifestType != null ? manifestType.entityClass : null;
    }

    public boolean isDeploymentRelated() {
        return manifestType == ManifestType.DEPLOYMENT ||
               manifestType == ManifestType.SERVICE ||
               manifestType == ManifestType.HPA;
    }

    public boolean isPersistenceRelated() {
        return manifestType == ManifestType.PERSISTENT_VOLUME_CLAIM;
    }

    public boolean isSecurityRelated() {
        return manifestType == ManifestType.SECRET ||
               manifestType == ManifestType.SERVICE_ACCOUNT ||
               manifestType == ManifestType.CLUSTER_ROLE;
    }

    public boolean isNetworkingRelated() {
        return manifestType == ManifestType.SERVICE ||
               manifestType == ManifestType.INGRESS;
    }

    public boolean isConfigRelated() {
        return manifestType == ManifestType.CONFIG_MAP ||
               manifestType == ManifestType.SECRET;
    }

    /**
     * Enum for Kubernetes Manifest Types
     * Maps to our entity classes and template files
     */
    public enum ManifestType {
        DEPLOYMENT("Deployment", "deployments.yaml", Deployment.class),
        SERVICE("Service", "services.yaml", K8sService.class),
        INGRESS("Ingress", "ingresses.yaml", Ingress.class),
        PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim", "persistentvolumeclaims.yaml", PersistentVolumeClaim.class),
        CONFIG_MAP("ConfigMap", "configmaps.yaml", ConfigMap.class),
        SECRET("Secret", "secret.yaml", Secret.class),
        SERVICE_ACCOUNT("ServiceAccount", "serviceaccounts.yaml", ServiceAccount.class),
        CLUSTER_ROLE("ClusterRole", "clusterroles.yaml", ClusterRole.class),
        HPA("HorizontalPodAutoscaler", "hpa.yaml", Hpa.class);

        final String kubernetesKind;
        final String templateFile;
        final Class<?> entityClass;

        ManifestType(String kubernetesKind, String templateFile, Class<?> entityClass) {
            this.kubernetesKind = kubernetesKind;
            this.templateFile = templateFile;
            this.entityClass = entityClass;
        }
    }
}