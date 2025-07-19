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
 * PersistentVolumeClaim Entity
 * Maps to Kubernetes PVC manifest properties
 */
@Entity
@Table(name = "config_persistent_volume_claims")
public class PersistentVolumeClaim extends PanacheEntity {
    
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
    @Column(name = "access_mode")
    public String accessMode = "ReadWriteOnce"; // ReadWriteOnce, ReadOnlyMany, ReadWriteMany
    
    @Column(name = "size")
    public String size; // e.g., "5Gi"
    
    @Column(name = "storage_class")
    public String storageClass; // Can be empty for default
    
    // Volume mode
    @Column(name = "volume_mode")
    public String volumeMode = "Filesystem"; // Filesystem, Block
    
    // Additional access modes
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_modes")
    public JsonNode accessModes;
    
    // Resource requirements
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resources")
    public JsonNode resources;
    
    // Selector
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selector")
    public JsonNode selector;
    
    // Volume name (if binding to specific PV)
    @Column(name = "volume_name")
    public String volumeName;
    
    // Data source
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_source")
    public JsonNode dataSource;
    
    // Annotations
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annotations")
    public JsonNode annotations;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Constructors
    public PersistentVolumeClaim() {}
    
    public PersistentVolumeClaim(App service, String metadataName, String component, String size) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
        this.size = size;
    }
    
    // Helper methods
    public boolean isReadWriteOnce() {
        return "ReadWriteOnce".equals(accessMode);
    }
    
    public boolean isReadOnlyMany() {
        return "ReadOnlyMany".equals(accessMode);
    }
    
    public boolean isReadWriteMany() {
        return "ReadWriteMany".equals(accessMode);
    }
    
    public boolean hasStorageClass() {
        return storageClass != null && !storageClass.isEmpty();
    }
    
    public boolean hasSelector() {
        return selector != null && !selector.isEmpty();
    }
    
    public boolean hasSpecificVolume() {
        return volumeName != null && !volumeName.isEmpty();
    }
    
    public boolean isBlockMode() {
        return "Block".equals(volumeMode);
    }
} 