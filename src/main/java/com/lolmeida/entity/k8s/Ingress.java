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
 * Ingress Entity
 * Maps to Kubernetes Ingress manifest properties
 */
@Entity
@Table(name = "config_ingresses")
public class Ingress extends BaseEntity {
    
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
    @Column(name = "ingress_class_name")
    public String ingressClassName; // e.g., "nginx"
    
    // Default backend
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_backend")
    public JsonNode defaultBackend;
    
    // Rules (hosts, paths, backends)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules")
    public JsonNode rules;
    
    // TLS configuration
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tls")
    public JsonNode tls;
    
    // Annotations (for controller-specific config)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annotations")
    public JsonNode annotations;
    
    // Host and path shortcuts (for simple cases)
    @Column(name = "primary_host")
    public String primaryHost;
    
    @Column(name = "primary_path")
    public String primaryPath = "/";
    
    @Column(name = "path_type")
    public String pathType = "Prefix"; // Exact, Prefix, ImplementationSpecific
    
    @Column(name = "target_service_name")
    public String targetServiceName;
    
    @Column(name = "target_service_port")
    public Integer targetServicePort;
    
    // SSL/TLS shortcuts
    @Column(name = "ssl_enabled")
    public Boolean sslEnabled = false;
    
    @Column(name = "tls_secret_name")
    public String tlsSecretName;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Constructors
    public Ingress() {}
    
    public Ingress(App service, String metadataName, String component, String primaryHost) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
        this.primaryHost = primaryHost;
    }
    
    // Helper methods
    public boolean hasMultipleRules() {
        return rules != null && rules.isArray() && rules.size() > 1;
    }
    
    public boolean hasTLS() {
        return sslEnabled && (tls != null && !tls.isEmpty()) || (tlsSecretName != null && !tlsSecretName.isEmpty());
    }
    
    public boolean hasDefaultBackend() {
        return defaultBackend != null && !defaultBackend.isEmpty();
    }
    
    public boolean isNginxIngress() {
        return "nginx".equals(ingressClassName);
    }
    
    public boolean isPathExact() {
        return "Exact".equals(pathType);
    }
    
    public boolean isPathPrefix() {
        return "Prefix".equals(pathType);
    }
    
    public boolean hasCustomAnnotations() {
        return annotations != null && !annotations.isEmpty();
    }
} 