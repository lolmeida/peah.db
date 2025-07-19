package com.lolmeida.peahdb.entity.k8s;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HPA (Horizontal Pod Autoscaler) Entity
 * Maps to Kubernetes HPA manifest properties
 */
@Entity
@Table(name = "config_hpa")
public class Hpa extends PanacheEntity {
    
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
    
    // Spec fields
    @Column(name = "component")
    public String component;
    
    @Column(name = "min_replicas")
    public Integer minReplicas = 1;
    
    @Column(name = "max_replicas") 
    public Integer maxReplicas = 5;
    
    @Column(name = "target_cpu_utilization_percentage")
    public Integer targetCPUUtilizationPercentage;
    
    @Column(name = "target_memory_utilization_percentage")
    public Integer targetMemoryUtilizationPercentage;
    
    // ScaleTargetRef
    @Column(name = "scale_target_ref_api_version")
    public String scaleTargetRefApiVersion = "apps/v1";
    
    @Column(name = "scale_target_ref_kind")
    public String scaleTargetRefKind = "Deployment";
    
    @Column(name = "scale_target_ref_name")
    public String scaleTargetRefName;
    
    // Behavior configuration (complex JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "behavior_config")
    public JsonNode behaviorConfig;
    
    // Additional metrics (for custom metrics)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_metrics")
    public JsonNode additionalMetrics;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at") 
    public LocalDateTime updatedAt;
    
    // Constructors
    public Hpa() {}
    
    public Hpa(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
        this.scaleTargetRefName = metadataName; // Usually same as metadata name
    }
    
    // Helper methods
    public boolean hasResourceMetrics() {
        return targetCPUUtilizationPercentage != null || targetMemoryUtilizationPercentage != null;
    }
    
    public boolean hasBehaviorConfig() {
        return behaviorConfig != null && !behaviorConfig.isEmpty();
    }
    
    public boolean hasCustomMetrics() {
        return additionalMetrics != null && !additionalMetrics.isEmpty();
    }
} 