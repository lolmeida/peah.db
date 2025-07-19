package com.lolmeida.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "ClusterRoleRequest",
    description = "ClusterRole creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-cluster-role",
      "component": "database",
      "createBinding": true,
      "bindingName": "postgresql-cluster-role-binding",
      "targetNamespace": "default",
      "targetServiceAccount": "postgresql-sa"
    }
    """
)
public class ClusterRoleRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the cluster role is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-cluster-role",
        required = true,
        maxLength = 100,
        pattern = "^[a-z0-9-]+$"
    )
    private String metadataName;
    
    @Schema(description = "Metadata labels JSON")
    private JsonNode metadataLabels;
    
    @Size(max = 50, message = "Component must not exceed 50 characters")
    @Schema(
        description = "Component name",
        example = "database",
        maxLength = 50
    )
    private String component;
    
    @Schema(description = "RBAC rules JSON array")
    private JsonNode rules;
    
    @Schema(description = "Whether to create a ClusterRoleBinding", example = "true")
    private Boolean createBinding;
    
    @Size(max = 100, message = "Binding name must not exceed 100 characters")
    @Schema(
        description = "ClusterRoleBinding name",
        example = "postgresql-cluster-role-binding",
        maxLength = 100
    )
    private String bindingName;
    
    @Size(max = 50, message = "Role ref API group must not exceed 50 characters")
    @Schema(
        description = "Role reference API group",
        example = "rbac.authorization.k8s.io",
        maxLength = 50
    )
    private String roleRefApiGroup;
    
    @Size(max = 30, message = "Role ref kind must not exceed 30 characters")
    @Schema(
        description = "Role reference kind",
        example = "ClusterRole",
        maxLength = 30
    )
    private String roleRefKind;
    
    @Size(max = 100, message = "Role ref name must not exceed 100 characters")
    @Schema(
        description = "Role reference name",
        example = "postgresql-cluster-role",
        maxLength = 100
    )
    private String roleRefName;
    
    @Schema(description = "Subjects JSON array (ServiceAccounts, Users, Groups)")
    private JsonNode subjects;
    
    @Size(max = 100, message = "Target namespace must not exceed 100 characters")
    @Schema(
        description = "Target namespace for ServiceAccount subjects",
        example = "default",
        maxLength = 100
    )
    private String targetNamespace;
    
    @Size(max = 100, message = "Target service account must not exceed 100 characters")
    @Schema(
        description = "Target service account name",
        example = "postgresql-sa",
        maxLength = 100
    )
    private String targetServiceAccount;
    
    @Schema(description = "Aggregation rule JSON for aggregated cluster roles")
    private JsonNode aggregationRule;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
} 