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
    name = "HpaResponse",
    description = "HPA (Horizontal Pod Autoscaler) response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-hpa",
      "component": "database",
      "minReplicas": 1,
      "maxReplicas": 5,
      "targetCPUUtilizationPercentage": 70,
      "scaleTargetRefName": "postgresql-deployment",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class HpaResponse {
    
    @Schema(
        description = "Unique identifier for the HPA",
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
        description = "Whether the HPA is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-hpa"
    )
    private String metadataName;
    
    @Schema(description = "Metadata labels JSON")
    private JsonNode metadataLabels;
    
    @Schema(
        description = "Component name",
        example = "database"
    )
    private String component;
    
    @Schema(
        description = "Minimum number of replicas",
        example = "1"
    )
    private Integer minReplicas;
    
    @Schema(
        description = "Maximum number of replicas",
        example = "5"
    )
    private Integer maxReplicas;
    
    @Schema(
        description = "Target CPU utilization percentage",
        example = "70"
    )
    private Integer targetCPUUtilizationPercentage;
    
    @Schema(
        description = "Target memory utilization percentage",
        example = "80"
    )
    private Integer targetMemoryUtilizationPercentage;
    
    @Schema(
        description = "Scale target reference API version",
        example = "apps/v1"
    )
    private String scaleTargetRefApiVersion;
    
    @Schema(
        description = "Scale target reference kind",
        example = "Deployment"
    )
    private String scaleTargetRefKind;
    
    @Schema(
        description = "Scale target reference name",
        example = "postgresql-deployment"
    )
    private String scaleTargetRefName;
    
    @Schema(description = "HPA behavior configuration JSON")
    private JsonNode behaviorConfig;
    
    @Schema(description = "Additional custom metrics JSON")
    private JsonNode additionalMetrics;
    
    @Schema(
        description = "Timestamp when the HPA was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the HPA was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 