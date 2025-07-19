package com.lolmeida.entity.k8s;

import com.lolmeida.entity.BaseEntity;
import com.lolmeida.entity.core.Stack;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * App Entity
 * Represents applications like N8N, PostgreSQL, Grafana, etc.
 * Defines which Kubernetes manifests each app requires
 */
@Entity
@Table(name = "config_apps")
public class App extends BaseEntity {
    
    // Reference to stack
    @ManyToOne
    @JoinColumn(name = "stack_id")
    public Stack stack;
    
    // Basic properties
    public Boolean enabled = false;
    
    // App identification
    @Column(name = "name", unique = true)
    public String name; // n8n, postgresql, redis, grafana, prometheus, peah-be
    
    @Column(name = "display_name")
    public String displayName; // "N8N Automation", "PostgreSQL Database"
    
    @Column(name = "description")
    public String description;
    
    @Column(name = "category")
    public String category; // database, monitoring, automation, api
    
    @Column(name = "version")
    public String version = "latest";
    
    // Docker image info
    @Column(name = "default_image_repository")
    public String defaultImageRepository;
    
    @Column(name = "default_image_tag")
    public String defaultImageTag = "latest";
    
    // Default configurations
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_config")
    public JsonNode defaultConfig; // Default values for this app
    
    // Required manifests for this app
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<AppManifest> requiredManifests = new ArrayList<>();
    
    // Dependencies on other apps
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dependencies")
    public JsonNode dependencies; // ["postgresql", "redis"] 
    
    // Ports exposed by this app
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_ports")
    public JsonNode defaultPorts;
    
    // Default resource requirements
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_resources")
    public JsonNode defaultResources;
    
    // Health check endpoints
    @Column(name = "health_check_path")
    public String healthCheckPath; // /health, /healthz, /q/health
    
    @Column(name = "readiness_check_path")  
    public String readinessCheckPath;
    
    // Documentation
    @Column(name = "documentation_url")
    public String documentationUrl;
    
    @Column(name = "icon_url")
    public String iconUrl;
    
    // Deployment priority (lower = deploy first)
    @Column(name = "deployment_priority")
    public Integer deploymentPriority = 100;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    // Constructors
    public App() {}
    
    public App(String name, String displayName, String category) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
    }
    
    // Helper methods
    public boolean isDatabaseApp() {
        return "database".equals(category);
    }
    
    public boolean isMonitoringApp() {
        return "monitoring".equals(category);
    }
    
    public boolean isApplicationApp() {
        return "automation".equals(category) || "api".equals(category);
    }
    
    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
    }
    
    public boolean hasHealthCheck() {
        return healthCheckPath != null && !healthCheckPath.isEmpty();
    }
    
    public boolean hasReadinessCheck() {
        return readinessCheckPath != null && !readinessCheckPath.isEmpty();
    }
    
    public String getFullImageName() {
        return defaultImageRepository + ":" + defaultImageTag;
    }
    
    public int getRequiredManifestsCount() {
        return requiredManifests != null ? requiredManifests.size() : 0;
    }
    
    public boolean requiresManifest(String manifestType) {
        if (requiredManifests == null) return false;
        return requiredManifests.stream()
                .anyMatch(manifest -> manifestType.equals(manifest.manifestType));
    }
    
    public boolean isPriorityApp() {
        return deploymentPriority != null && deploymentPriority < 50;
    }
} 