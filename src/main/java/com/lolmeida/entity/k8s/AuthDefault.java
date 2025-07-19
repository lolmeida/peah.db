package com.lolmeida.entity.k8s;

import com.fasterxml.jackson.databind.JsonNode;
import com.lolmeida.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Auth Default entity for storing default authentication configurations by category and type
 * Maps to config_auth_defaults table
 */
@Entity
@Table(name = "config_auth_defaults")
@Data
@EqualsAndHashCode(callSuper = false, exclude = {"category"})
@ToString(exclude = {"category"})
public class AuthDefault extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(name = "auth_type", nullable = false, length = 50)
    private String authType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_config", columnDefinition = "JSON", nullable = false)
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
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public boolean isPasswordAuth() {
        return "password".equalsIgnoreCase(this.authType);
    }

    public boolean isJwtAuth() {
        return "jwt".equalsIgnoreCase(this.authType);
    }

    public boolean isBasicAuth() {
        return "basic".equalsIgnoreCase(this.authType);
    }

    public boolean isEmailAuth() {
        return "email".equalsIgnoreCase(this.authType);
    }

    public boolean isOAuth2Auth() {
        return "oauth2".equalsIgnoreCase(this.authType);
    }

    public boolean isLdapAuth() {
        return "ldap".equalsIgnoreCase(this.authType);
    }

    public boolean isCertificateAuth() {
        return "certificate".equalsIgnoreCase(this.authType);
    }

    public boolean isApiKeyAuth() {
        return "api-key".equalsIgnoreCase(this.authType);
    }

    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }

    public boolean hasDefaultConfig() {
        return defaultConfig != null && !defaultConfig.isEmpty();
    }
}
