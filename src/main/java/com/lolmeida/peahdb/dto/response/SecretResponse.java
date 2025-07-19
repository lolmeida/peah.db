package com.lolmeida.peahdb.dto.response;

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
    name = "SecretResponse",
    description = "Secret response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-secret",
      "component": "database",
      "type": "Opaque",
      "immutable": false,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class SecretResponse {
    
    @Schema(
        description = "Unique identifier for the secret",
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
        description = "Whether the secret is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-secret"
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
        description = "Type of secret",
        example = "Opaque"
    )
    private String type;
    
    @Schema(description = "Secret data (base64 encoded) JSON")
    private JsonNode data;
    
    @Schema(description = "Secret string data (plain text) JSON")
    private JsonNode stringData;
    
    @Schema(
        description = "Whether the secret is immutable",
        example = "false"
    )
    private Boolean immutable;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Timestamp when the secret was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the secret was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 