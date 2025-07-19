package com.lolmeida.entity.k8s;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Manifest Default entity for storing default manifest configurations by category
 * Maps to config_manifest_defaults table
 */
@Entity
@Table(name = "config_manifest_defaults")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"category"})
@ToString(exclude = {"category"})
public class ManifestDefault extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(name = "manifest_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AppManifest.ManifestType manifestType;

    @Column(name = "required")
    private Boolean required = true;

    @Column(name = "creation_priority")
    private Integer creationPriority = 100;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "creation_condition", length = 200)
    private String creationCondition;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_config", columnDefinition = "JSON")
    private JsonNode defaultConfig;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Utility methods
    public boolean isRequired() {
        return Boolean.TRUE.equals(this.required);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public boolean hasCondition() {
        return creationCondition != null && !creationCondition.trim().isEmpty();
    }

    public boolean hasDefaultConfig() {
        return defaultConfig != null && !defaultConfig.isEmpty();
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public String getManifestTypeName() {
        return manifestType != null ? manifestType.name() : null;
    }
}
