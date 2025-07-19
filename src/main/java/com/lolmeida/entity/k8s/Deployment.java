package com.lolmeida.entity.k8s;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deployment Entity
 * Maps to Kubernetes Deployment manifest properties
 */
@Entity
@Table(name = "config_deployments")
public class Deployment extends PanacheEntity {
    
    // Reference to service
    @ManyToOne
    @JoinColumn(name = "service_id")
    public App service;
    
    // Basic properties
    public Boolean enabled = false;
    
    // Metadata fields
    @Column(name = "metadata_name")
    public String metadataName;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_labels")
    public JsonNode metadataLabels;
    
    @Column(name = "component")
    public String component;
    
    // Spec fields
    @Column(name = "replica_count")
    public Integer replicaCount = 1;
    
    // Image configuration
    @Column(name = "image_repository")
    public String imageRepository;
    
    @Column(name = "image_tag")
    public String imageTag = "latest";
    
    @Column(name = "image_pull_policy")
    public String imagePullPolicy = "IfNotPresent";
    
    // Port configuration
    @Column(name = "port_name")
    public String portName = "http";
    
    @Column(name = "container_port")
    public Integer containerPort;
    
    @Column(name = "protocol")
    public String protocol = "TCP";
    
    // Service Account
    @Column(name = "service_account_name")
    public String serviceAccountName;
    
    // Command and Args
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "command")
    public JsonNode command;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "args")
    public JsonNode args;
    
    // Environment variables
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "env_vars")
    public JsonNode envVars;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "env_from")
    public JsonNode envFrom;
    
    // Resources
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resources")
    public JsonNode resources;
    
    // Volume mounts
    @Column(name = "volume_mount_name")
    public String volumeMountName;
    
    @Column(name = "volume_mount_path")
    public String volumeMountPath;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_volume_mounts")
    public JsonNode extraVolumeMounts;
    
    // Extra volumes
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_volumes")
    public JsonNode extraVolumes;
    
    // Probes
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "liveness_probe")
    public JsonNode livenessProbe;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "readiness_probe")
    public JsonNode readinessProbe;
    
    // Node selection
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "node_selector")
    public JsonNode nodeSelector;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "affinity")
    public JsonNode affinity;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tolerations")
    public JsonNode tolerations;
    
    // Persistence reference
    @Column(name = "persistence_enabled")
    public Boolean persistenceEnabled = false;
    
    // Security context
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "security_context")
    public JsonNode securityContext;
    
    // Pod Security context
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pod_security_context")
    public JsonNode podSecurityContext;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Constructors
    public Deployment() {}
    
    public Deployment(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
    }
    
    // Helper methods
    public String getFullImageName() {
        return imageRepository + ":" + imageTag;
    }
    
    public boolean hasCustomCommand() {
        return command != null && !command.isEmpty();
    }
    
    public boolean hasCustomArgs() {
        return args != null && !args.isEmpty();
    }
    
    public boolean hasProbes() {
        return (livenessProbe != null && !livenessProbe.isEmpty()) || 
               (readinessProbe != null && !readinessProbe.isEmpty());
    }
    
    public boolean hasExtraVolumes() {
        return extraVolumes != null && !extraVolumes.isEmpty();
    }
    
    public boolean hasResourceLimits() {
        return resources != null && !resources.isEmpty();
    }
} 