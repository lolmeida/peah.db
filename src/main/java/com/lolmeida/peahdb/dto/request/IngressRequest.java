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
    name = "IngressRequest",
    description = "Ingress creation and update request data",
    example = """
    {
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
      "sslEnabled": true
    }
    """
)
public class IngressRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the ingress is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-ingress",
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
    
    @Size(max = 50, message = "Ingress class name must not exceed 50 characters")
    @Schema(
        description = "Ingress class name",
        example = "nginx",
        maxLength = 50
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
    
    @Size(max = 100, message = "Primary host must not exceed 100 characters")
    @Schema(
        description = "Primary hostname",
        example = "db.example.com",
        maxLength = 100
    )
    private String primaryHost;
    
    @Size(max = 100, message = "Primary path must not exceed 100 characters")
    @Schema(
        description = "Primary path",
        example = "/",
        maxLength = 100
    )
    private String primaryPath;
    
    @Size(max = 30, message = "Path type must not exceed 30 characters")
    @Schema(
        description = "Path type",
        example = "Prefix",
        maxLength = 30,
        enumeration = {"Exact", "Prefix", "ImplementationSpecific"}
    )
    private String pathType;
    
    @Size(max = 100, message = "Target service name must not exceed 100 characters")
    @Schema(
        description = "Target service name",
        example = "postgresql-service",
        maxLength = 100
    )
    private String targetServiceName;
    
    @Min(value = 1, message = "Target service port must be at least 1")
    @Max(value = 65535, message = "Target service port must not exceed 65535")
    @Schema(
        description = "Target service port",
        example = "5432",
        minimum = "1",
        maximum = "65535"
    )
    private Integer targetServicePort;
    
    @Schema(description = "Whether SSL is enabled", example = "true")
    private Boolean sslEnabled;
    
    @Size(max = 100, message = "TLS secret name must not exceed 100 characters")
    @Schema(
        description = "TLS secret name",
        example = "db-tls-secret",
        maxLength = 100
    )
    private String tlsSecretName;
} 