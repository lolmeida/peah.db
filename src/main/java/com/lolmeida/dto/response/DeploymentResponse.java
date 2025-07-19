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
    name = "DeploymentResponse",
    description = "Deployment response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-deployment",
      "component": "database",
      "replicaCount": 1,
      "imageRepository": "postgres",
      "imageTag": "15",
      "containerPort": 5432,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class DeploymentResponse {
    
    @Schema(
        description = "Unique identifier for the deployment",
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
        description = "Whether the deployment is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-deployment"
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
        description = "Number of replicas",
        example = "1"
    )
    private Integer replicaCount;
    
    @Schema(
        description = "Docker image repository",
        example = "postgres"
    )
    private String imageRepository;
    
    @Schema(
        description = "Docker image tag",
        example = "15"
    )
    private String imageTag;
    
    @Schema(
        description = "Image pull policy",
        example = "IfNotPresent"
    )
    private String imagePullPolicy;
    
    @Schema(
        description = "Port name",
        example = "http"
    )
    private String portName;
    
    @Schema(
        description = "Container port number",
        example = "5432"
    )
    private Integer containerPort;
    
    @Schema(
        description = "Protocol",
        example = "TCP"
    )
    private String protocol;
    
    @Schema(
        description = "Service account name",
        example = "postgresql-sa"
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
    
    @Schema(
        description = "Volume mount name",
        example = "data-volume"
    )
    private String volumeMountName;
    
    @Schema(
        description = "Volume mount path",
        example = "/var/lib/postgresql/data"
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
    
    @Schema(
        description = "Whether persistence is enabled",
        example = "false"
    )
    private Boolean persistenceEnabled;
    
    @Schema(description = "Security context JSON")
    private JsonNode securityContext;
    
    @Schema(description = "Pod security context JSON")
    private JsonNode podSecurityContext;
    
    @Schema(
        description = "Timestamp when the deployment was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the deployment was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 