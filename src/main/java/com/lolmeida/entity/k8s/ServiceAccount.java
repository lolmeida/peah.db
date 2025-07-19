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
 * ServiceAccount Entity
 * Maps to Kubernetes ServiceAccount manifest properties
 */
@Entity
@Table(name = "config_service_accounts")
public class ServiceAccount extends BaseEntity {
    
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
    
    // ServiceAccount specific fields
    @Column(name = "automount_service_account_token")
    public Boolean automountServiceAccountToken;
    
    // Image pull secrets
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_pull_secrets")
    public JsonNode imagePullSecrets;
    
    // Secrets
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "secrets")
    public JsonNode secrets;
    
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
    public ServiceAccount() {}
    
    public ServiceAccount(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
    }
    
    // Helper methods
    public boolean hasImagePullSecrets() {
        return imagePullSecrets != null && !imagePullSecrets.isEmpty();
    }
    
    public boolean hasSecrets() {
        return secrets != null && !secrets.isEmpty();
    }
    
    public boolean isTokenAutoMounted() {
        return automountServiceAccountToken == null || automountServiceAccountToken;
    }
    
    public boolean hasCustomAnnotations() {
        return annotations != null && !annotations.isEmpty();
    }
} 