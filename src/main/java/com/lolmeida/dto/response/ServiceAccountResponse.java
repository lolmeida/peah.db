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
    name = "ServiceAccountResponse",
    description = "ServiceAccount response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-sa",
      "component": "database",
      "automountServiceAccountToken": true,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class ServiceAccountResponse {
    
    @Schema(
        description = "Unique identifier for the service account",
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
        description = "Whether the service account is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-sa"
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
        description = "Whether to automount service account token",
        example = "true"
    )
    private Boolean automountServiceAccountToken;
    
    @Schema(description = "Image pull secrets JSON array")
    private JsonNode imagePullSecrets;
    
    @Schema(description = "Secrets JSON array")
    private JsonNode secrets;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Timestamp when the service account was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the service account was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 