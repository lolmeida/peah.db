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
    name = "K8sServiceRequest",
    description = "Kubernetes Service creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-service",
      "component": "database",
      "type": "ClusterIP",
      "port": 5432,
      "targetPort": "5432",
      "protocol": "TCP"
    }
    """
)
public class K8sServiceRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the service is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-service",
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
    
    @Size(max = 20, message = "Service type must not exceed 20 characters")
    @Schema(
        description = "Service type",
        example = "ClusterIP",
        maxLength = 20,
        enumeration = {"ClusterIP", "NodePort", "LoadBalancer", "ExternalName"}
    )
    private String type;
    
    @Min(value = 1, message = "Port must be at least 1")
    @Max(value = 65535, message = "Port must not exceed 65535")
    @Schema(
        description = "Service port number",
        example = "5432",
        minimum = "1",
        maximum = "65535"
    )
    private Integer port;
    
    @Size(max = 20, message = "Target port must not exceed 20 characters")
    @Schema(
        description = "Target port (can be number or name)",
        example = "5432",
        maxLength = 20
    )
    private String targetPort;
    
    @Size(max = 10, message = "Protocol must not exceed 10 characters")
    @Schema(
        description = "Protocol",
        example = "TCP",
        maxLength = 10
    )
    private String protocol;
    
    @Size(max = 50, message = "Port name must not exceed 50 characters")
    @Schema(
        description = "Port name",
        example = "postgresql",
        maxLength = 50
    )
    private String portName;
    
    @Schema(description = "Additional ports JSON array")
    private JsonNode additionalPorts;
    
    @Schema(description = "Selector JSON")
    private JsonNode selector;
    
    @Size(max = 20, message = "Session affinity must not exceed 20 characters")
    @Schema(
        description = "Session affinity",
        example = "None",
        maxLength = 20,
        enumeration = {"None", "ClientIP"}
    )
    private String sessionAffinity;
    
    @Schema(description = "Session affinity configuration JSON")
    private JsonNode sessionAffinityConfig;
    
    @Size(max = 20, message = "External traffic policy must not exceed 20 characters")
    @Schema(
        description = "External traffic policy",
        example = "Cluster",
        maxLength = 20,
        enumeration = {"Cluster", "Local"}
    )
    private String externalTrafficPolicy;
    
    @Size(max = 50, message = "Load balancer IP must not exceed 50 characters")
    @Schema(
        description = "Load balancer IP address",
        example = "192.168.1.100",
        maxLength = 50
    )
    private String loadBalancerIP;
    
    @Schema(description = "Load balancer source ranges JSON array")
    private JsonNode loadBalancerSourceRanges;
    
    @Size(max = 100, message = "External name must not exceed 100 characters")
    @Schema(
        description = "External name for ExternalName type service",
        example = "external-db.example.com",
        maxLength = 100
    )
    private String externalName;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
} 