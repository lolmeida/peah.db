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
 * ConfigMap Entity
 * Maps to Kubernetes ConfigMap manifest properties
 */
@Entity
@Table(name = "config_configmaps")
public class ConfigMap extends PanacheEntity {
    
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
    
    // Data fields
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data")
    public JsonNode data; // Key-value pairs of configuration data
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "binary_data")
    public JsonNode binaryData; // Binary data (base64 encoded)
    
    // Immutable flag
    @Column(name = "immutable")
    public Boolean immutable = false;
    
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
    public ConfigMap() {}
    
    public ConfigMap(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
    }
    
    // Helper methods
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
    
    public boolean hasBinaryData() {
        return binaryData != null && !binaryData.isEmpty();
    }
    
    public boolean isImmutable() {
        return immutable != null && immutable;
    }
    
    public int getDataSize() {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        return data.size();
    }
} 