package com.lolmeida.peahdb.dto.request;

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
    name = "HpaRequest",
    description = "HPA (Horizontal Pod Autoscaler) creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-hpa",
      "component": "database",
      "minReplicas": 1,
      "maxReplicas": 5,
      "targetCPUUtilizationPercentage": 70,
      "scaleTargetRefName": "postgresql-deployment"
    }
    """
)
public class HpaRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the HPA is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-hpa",
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
    
    @Min(value = 1, message = "Min replicas must be at least 1")
    @Max(value = 100, message = "Min replicas must not exceed 100")
    @Schema(
        description = "Minimum number of replicas",
        example = "1",
        minimum = "1",
        maximum = "100"
    )
    private Integer minReplicas;
    
    @Min(value = 1, message = "Max replicas must be at least 1")
    @Max(value = 1000, message = "Max replicas must not exceed 1000")
    @Schema(
        description = "Maximum number of replicas",
        example = "5",
        minimum = "1",
        maximum = "1000"
    )
    private Integer maxReplicas;
    
    @Min(value = 1, message = "Target CPU utilization must be at least 1%")
    @Max(value = 100, message = "Target CPU utilization must not exceed 100%")
    @Schema(
        description = "Target CPU utilization percentage",
        example = "70",
        minimum = "1",
        maximum = "100"
    )
    private Integer targetCPUUtilizationPercentage;
    
    @Min(value = 1, message = "Target memory utilization must be at least 1%")
    @Max(value = 100, message = "Target memory utilization must not exceed 100%")
    @Schema(
        description = "Target memory utilization percentage",
        example = "80",
        minimum = "1",
        maximum = "100"
    )
    private Integer targetMemoryUtilizationPercentage;
    
    @Size(max = 20, message = "Scale target ref API version must not exceed 20 characters")
    @Schema(
        description = "Scale target reference API version",
        example = "apps/v1",
        maxLength = 20
    )
    private String scaleTargetRefApiVersion;
    
    @Size(max = 20, message = "Scale target ref kind must not exceed 20 characters")
    @Schema(
        description = "Scale target reference kind",
        example = "Deployment",
        maxLength = 20
    )
    private String scaleTargetRefKind;
    
    @NotBlank(message = "Scale target ref name is required")
    @Size(max = 100, message = "Scale target ref name must not exceed 100 characters")
    @Schema(
        description = "Scale target reference name",
        example = "postgresql-deployment",
        required = true,
        maxLength = 100
    )
    private String scaleTargetRefName;
    
    @Schema(description = "HPA behavior configuration JSON")
    private JsonNode behaviorConfig;
    
    @Schema(description = "Additional custom metrics JSON")
    private JsonNode additionalMetrics;
} 