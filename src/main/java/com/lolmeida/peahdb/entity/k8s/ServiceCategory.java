package com.lolmeida.peahdb.entity.k8s;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Category entity for grouping services by type
 * Maps to config_service_categories table
 */
@Entity
@Table(name = "config_service_categories")
@Data
@EqualsAndHashCode(exclude = {"manifestDefaults", "authDefaults"})
@ToString(exclude = {"manifestDefaults", "authDefaults"})
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "color", length = 20)
    private String color = "default";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ManifestDefault> manifestDefaults;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AuthDefault> authDefaults;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public String getCategoryKey() {
        return this.name;
    }

    public boolean isDatabase() {
        return "database".equalsIgnoreCase(this.name);
    }

    public boolean isMonitoring() {
        return "monitoring".equalsIgnoreCase(this.name);
    }

    public boolean isAutomation() {
        return "automation".equalsIgnoreCase(this.name);
    }

    public boolean isApi() {
        return "api".equalsIgnoreCase(this.name);
    }

    public boolean isMessageQueue() {
        return "messagequeue".equalsIgnoreCase(this.name);
    }

    public boolean isDefault() {
        return "default".equalsIgnoreCase(this.name);
    }
}
