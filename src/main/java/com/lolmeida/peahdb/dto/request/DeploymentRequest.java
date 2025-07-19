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
    name = "DeploymentRequest",
    description = "Deployment creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-deployment",
      "component": "database",
      "replicaCount": 1,
      "imageRepository": "postgres",
      "imageTag": "15",
      "containerPort": 5432
    }
    """
)
public class DeploymentRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the deployment is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-deployment",
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
    
    @Min(value = 0, message = "Replica count cannot be negative")
    @Max(value = 100, message = "Replica count cannot exceed 100")
    @Schema(
        description = "Number of replicas",
        example = "1",
        minimum = "0",
        maximum = "100"
    )
    private Integer replicaCount;
    
    @Size(max = 200, message = "Image repository must not exceed 200 characters")
    @Schema(
        description = "Docker image repository",
        example = "postgres",
        maxLength = 200
    )
    private String imageRepository;
    
    @Size(max = 50, message = "Image tag must not exceed 50 characters")
    @Schema(
        description = "Docker image tag",
        example = "15",
        maxLength = 50
    )
    private String imageTag;
    
    @Size(max = 20, message = "Image pull policy must not exceed 20 characters")
    @Schema(
        description = "Image pull policy",
        example = "IfNotPresent",
        maxLength = 20
    )
    private String imagePullPolicy;
    
    @Size(max = 50, message = "Port name must not exceed 50 characters")
    @Schema(
        description = "Port name",
        example = "http",
        maxLength = 50
    )
    private String portName;
    
    @Min(value = 1, message = "Container port must be at least 1")
    @Max(value = 65535, message = "Container port must not exceed 65535")
    @Schema(
        description = "Container port number",
        example = "5432",
        minimum = "1",
        maximum = "65535"
    )
    private Integer containerPort;
    
    @Size(max = 10, message = "Protocol must not exceed 10 characters")
    @Schema(
        description = "Protocol",
        example = "TCP",
        maxLength = 10
    )
    private String protocol;
    
    @Size(max = 100, message = "Service account name must not exceed 100 characters")
    @Schema(
        description = "Service account name",
        example = "postgresql-sa",
        maxLength = 100
    )
    private String serviceAccountName;
    
    @Schema(description = "Command JSON array")
    private JsonNode command;
    
    @Schema(description = "Args JSON array")
    private JsonNode args;
    
    @Schema(description = "Environment variables JSON")
    private JsonNode envVars;
    
    @Schema(description = "Environment from JSON")
    private JsonNode envFrom;
    
    @Schema(description = "Resource requirements JSON")
    private JsonNode resources;
    
    @Size(max = 100, message = "Volume mount name must not exceed 100 characters")
    @Schema(
        description = "Volume mount name",
        example = "data-volume",
        maxLength = 100
    )
    private String volumeMountName;
    
    @Size(max = 200, message = "Volume mount path must not exceed 200 characters")
    @Schema(
        description = "Volume mount path",
        example = "/var/lib/postgresql/data",
        maxLength = 200
    )
    private String volumeMountPath;
    
    @Schema(description = "Extra volume mounts JSON")
    private JsonNode extraVolumeMounts;
    
    @Schema(description = "Extra volumes JSON")
    private JsonNode extraVolumes;
    
    @Schema(description = "Liveness probe configuration JSON")
    private JsonNode livenessProbe;
    
    @Schema(description = "Readiness probe configuration JSON")
    private JsonNode readinessProbe;
    
    @Schema(description = "Node selector JSON")
    private JsonNode nodeSelector;
    
    @Schema(description = "Affinity rules JSON")
    private JsonNode affinity;
    
    @Schema(description = "Tolerations JSON")
    private JsonNode tolerations;
    
    @Schema(description = "Whether persistence is enabled", example = "false")
    private Boolean persistenceEnabled;
    
    @Schema(description = "Security context JSON")
    private JsonNode securityContext;
    
    @Schema(description = "Pod security context JSON")
    private JsonNode podSecurityContext;
} 