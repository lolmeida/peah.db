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
 * Secret Entity
 * Maps to Kubernetes Secret manifest properties
 */
@Entity
@Table(name = "config_secrets")
public class Secret extends PanacheEntity {
    
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
    
    // Type of secret
    @Column(name = "secret_type")
    public String type = "Opaque"; // Opaque, kubernetes.io/tls, kubernetes.io/dockerconfigjson, etc.
    
    // Data fields (base64 encoded)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data")
    public JsonNode data; // Key-value pairs of secret data (base64 encoded)
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "string_data")
    public JsonNode stringData; // Key-value pairs of secret data (plain text)
    
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
    public Secret() {}
    
    public Secret(App service, String metadataName, String component, String type) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
        this.type = type;
    }
    
    // Helper methods
    public boolean isOpaque() {
        return "Opaque".equals(type);
    }
    
    public boolean isTLSSecret() {
        return "kubernetes.io/tls".equals(type);
    }
    
    public boolean isDockerConfigSecret() {
        return "kubernetes.io/dockerconfigjson".equals(type);
    }
    
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
    
    public boolean hasStringData() {
        return stringData != null && !stringData.isEmpty();
    }
    
    public boolean isImmutable() {
        return immutable != null && immutable;
    }
    
    public int getSecretSize() {
        int size = 0;
        if (data != null && !data.isEmpty()) {
            size += data.size();
        }
        if (stringData != null && !stringData.isEmpty()) {
            size += stringData.size();
        }
        return size;
    }
} 