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
 * Kubernetes Service Entity
 * Maps to Kubernetes Service manifest properties
 */
@Entity
@Table(name = "config_kubernetes_services")
public class K8sService extends PanacheEntity {
    
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
    @Column(name = "service_type")
    public String type = "ClusterIP"; // ClusterIP, NodePort, LoadBalancer, ExternalName
    
    @Column(name = "port")
    public Integer port;
    
    @Column(name = "target_port")
    public String targetPort; // Can be number or name
    
    @Column(name = "protocol")
    public String protocol = "TCP";
    
    @Column(name = "port_name")
    public String portName;
    
    // Additional ports
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_ports")
    public JsonNode additionalPorts;
    
    // Selector (matches deployment labels)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selector")
    public JsonNode selector;
    
    // Session affinity
    @Column(name = "session_affinity")
    public String sessionAffinity; // None, ClientIP
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "session_affinity_config")
    public JsonNode sessionAffinityConfig;
    
    // External traffic policy (for NodePort and LoadBalancer)
    @Column(name = "external_traffic_policy")
    public String externalTrafficPolicy; // Cluster, Local
    
    // LoadBalancer specific
    @Column(name = "load_balancer_ip")
    public String loadBalancerIP;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "load_balancer_source_ranges")
    public JsonNode loadBalancerSourceRanges;
    
    // ExternalName specific
    @Column(name = "external_name")
    public String externalName;
    
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
    public K8sService() {}
    
    public K8sService(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
    }
    
    // Helper methods
    public boolean isClusterIP() {
        return "ClusterIP".equals(type);
    }
    
    public boolean isNodePort() {
        return "NodePort".equals(type);
    }
    
    public boolean isLoadBalancer() {
        return "LoadBalancer".equals(type);
    }
    
    public boolean isExternalName() {
        return "ExternalName".equals(type);
    }
    
    public boolean hasMultiplePorts() {
        return additionalPorts != null && !additionalPorts.isEmpty();
    }
    
    public boolean hasSessionAffinity() {
        return "ClientIP".equals(sessionAffinity);
    }
} 