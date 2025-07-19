package com.lolmeida.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ClusterRoleResponse",
    description = "ClusterRole response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-cluster-role",
      "component": "database",
      "createBinding": true,
      "bindingName": "postgresql-cluster-role-binding",
      "targetNamespace": "default",
      "targetServiceAccount": "postgresql-sa",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class ClusterRoleResponse {
    
    @Schema(
        description = "Unique identifier for the cluster role",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "Reference to service (App) ID",
        example = "1"
    )
    private Long serviceId;
    
    @Schema(
        description = "Whether the cluster role is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-cluster-role"
    )
    private String metadataName;
    
    @Schema(description = "Metadata labels JSON")
    private JsonNode metadataLabels;
    
    @Schema(
        description = "Component name",
        example = "database"
    )
    private String component;
    
    @Schema(description = "RBAC rules JSON array")
    private JsonNode rules;
    
    @Schema(
        description = "Whether to create a ClusterRoleBinding",
        example = "true"
    )
    private Boolean createBinding;
    
    @Schema(
        description = "ClusterRoleBinding name",
        example = "postgresql-cluster-role-binding"
    )
    private String bindingName;
    
    @Schema(
        description = "Role reference API group",
        example = "rbac.authorization.k8s.io"
    )
    private String roleRefApiGroup;
    
    @Schema(
        description = "Role reference kind",
        example = "ClusterRole"
    )
    private String roleRefKind;
    
    @Schema(
        description = "Role reference name",
        example = "postgresql-cluster-role"
    )
    private String roleRefName;
    
    @Schema(description = "Subjects JSON array (ServiceAccounts, Users, Groups)")
    private JsonNode subjects;
    
    @Schema(
        description = "Target namespace for ServiceAccount subjects",
        example = "default"
    )
    private String targetNamespace;
    
    @Schema(
        description = "Target service account name",
        example = "postgresql-sa"
    )
    private String targetServiceAccount;
    
    @Schema(description = "Aggregation rule JSON for aggregated cluster roles")
    private JsonNode aggregationRule;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Timestamp when the cluster role was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the cluster role was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 