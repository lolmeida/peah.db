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
    name = "IngressResponse",
    description = "Ingress response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-ingress",
      "component": "database",
      "ingressClassName": "nginx",
      "primaryHost": "db.example.com",
      "primaryPath": "/",
      "pathType": "Prefix",
      "targetServiceName": "postgresql-service",
      "targetServicePort": 5432,
      "sslEnabled": true,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class IngressResponse {
    
    @Schema(
        description = "Unique identifier for the ingress",
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
        description = "Whether the ingress is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-ingress"
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
        description = "Ingress class name",
        example = "nginx"
    )
    private String ingressClassName;
    
    @Schema(description = "Default backend JSON")
    private JsonNode defaultBackend;
    
    @Schema(description = "Ingress rules JSON")
    private JsonNode rules;
    
    @Schema(description = "TLS configuration JSON")
    private JsonNode tls;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Primary hostname",
        example = "db.example.com"
    )
    private String primaryHost;
    
    @Schema(
        description = "Primary path",
        example = "/"
    )
    private String primaryPath;
    
    @Schema(
        description = "Path type",
        example = "Prefix"
    )
    private String pathType;
    
    @Schema(
        description = "Target service name",
        example = "postgresql-service"
    )
    private String targetServiceName;
    
    @Schema(
        description = "Target service port",
        example = "5432"
    )
    private Integer targetServicePort;
    
    @Schema(
        description = "Whether SSL is enabled",
        example = "true"
    )
    private Boolean sslEnabled;
    
    @Schema(
        description = "TLS secret name",
        example = "db-tls-secret"
    )
    private String tlsSecretName;
    
    @Schema(
        description = "Timestamp when the ingress was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the ingress was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 