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
    name = "K8sServiceResponse",
    description = "Kubernetes Service response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-service",
      "component": "database",
      "type": "ClusterIP",
      "port": 5432,
      "targetPort": "5432",
      "protocol": "TCP",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class K8sServiceResponse {
    
    @Schema(
        description = "Unique identifier for the service",
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
        description = "Whether the service is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-service"
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
        description = "Service type",
        example = "ClusterIP"
    )
    private String type;
    
    @Schema(
        description = "Service port number",
        example = "5432"
    )
    private Integer port;
    
    @Schema(
        description = "Target port (can be number or name)",
        example = "5432"
    )
    private String targetPort;
    
    @Schema(
        description = "Protocol",
        example = "TCP"
    )
    private String protocol;
    
    @Schema(
        description = "Port name",
        example = "postgresql"
    )
    private String portName;
    
    @Schema(description = "Additional ports JSON array")
    private JsonNode additionalPorts;
    
    @Schema(description = "Selector JSON")
    private JsonNode selector;
    
    @Schema(
        description = "Session affinity",
        example = "None"
    )
    private String sessionAffinity;
    
    @Schema(description = "Session affinity configuration JSON")
    private JsonNode sessionAffinityConfig;
    
    @Schema(
        description = "External traffic policy",
        example = "Cluster"
    )
    private String externalTrafficPolicy;
    
    @Schema(
        description = "Load balancer IP address",
        example = "192.168.1.100"
    )
    private String loadBalancerIP;
    
    @Schema(description = "Load balancer source ranges JSON array")
    private JsonNode loadBalancerSourceRanges;
    
    @Schema(
        description = "External name for ExternalName type service",
        example = "external-db.example.com"
    )
    private String externalName;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Timestamp when the service was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the service was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 