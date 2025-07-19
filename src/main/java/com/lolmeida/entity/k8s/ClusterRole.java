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
 * ClusterRole Entity
 * Maps to Kubernetes ClusterRole and ClusterRoleBinding manifest properties
 */
@Entity
@Table(name = "config_cluster_roles")
public class ClusterRole extends PanacheEntity {
    
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
    
    // ClusterRole Rules
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rules")
    public JsonNode rules; // Array of rules with apiGroups, resources, verbs, nonResourceURLs
    
    // ClusterRoleBinding fields
    @Column(name = "create_binding")
    public Boolean createBinding = true;
    
    @Column(name = "binding_name")
    public String bindingName;
    
    // RoleRef (usually references this ClusterRole)
    @Column(name = "role_ref_api_group")
    public String roleRefApiGroup = "rbac.authorization.k8s.io";
    
    @Column(name = "role_ref_kind") 
    public String roleRefKind = "ClusterRole";
    
    @Column(name = "role_ref_name")
    public String roleRefName;
    
    // Subjects (ServiceAccounts, Users, Groups)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "subjects")
    public JsonNode subjects;
    
    // Target namespace for ServiceAccount subjects
    @Column(name = "target_namespace")
    public String targetNamespace;
    
    // Target service account name (shortcut for common case)
    @Column(name = "target_service_account")
    public String targetServiceAccount;
    
    // Aggregation rule (for aggregated cluster roles)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aggregation_rule")
    public JsonNode aggregationRule;
    
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
    public ClusterRole() {}
    
    public ClusterRole(App service, String metadataName, String component) {
        this.service = service;
        this.metadataName = metadataName;
        this.component = component;
        this.roleRefName = metadataName; // Default: role ref points to itself
        this.bindingName = metadataName; // Default: same name for binding
    }
    
    // Helper methods
    public boolean hasRules() {
        return rules != null && !rules.isEmpty();
    }
    
    public boolean hasBinding() {
        return createBinding != null && createBinding;
    }
    
    public boolean hasSubjects() {
        return subjects != null && !subjects.isEmpty();
    }
    
    public boolean isAggregatedRole() {
        return aggregationRule != null && !aggregationRule.isEmpty();
    }
    
    public boolean targetsServiceAccount() {
        return targetServiceAccount != null && !targetServiceAccount.isEmpty();
    }
    
    public boolean hasCustomRoleRef() {
        return !metadataName.equals(roleRefName);
    }
    
    public int getRulesCount() {
        if (rules == null || !rules.isArray()) {
            return 0;
        }
        return rules.size();
    }
    
    public int getSubjectsCount() {
        if (subjects == null || !subjects.isArray()) {
            return 0;
        }
        return subjects.size();
    }
} 