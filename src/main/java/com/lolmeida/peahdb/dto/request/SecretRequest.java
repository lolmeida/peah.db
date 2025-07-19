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
    name = "SecretRequest",
    description = "Secret creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-secret",
      "component": "database",
      "type": "Opaque",
      "immutable": false
    }
    """
)
public class SecretRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the secret is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-secret",
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
    
    @Size(max = 100, message = "Secret type must not exceed 100 characters")
    @Schema(
        description = "Type of secret",
        example = "Opaque",
        maxLength = 100
    )
    private String type;
    
    @Schema(description = "Secret data (base64 encoded) JSON")
    private JsonNode data;
    
    @Schema(description = "Secret string data (plain text) JSON")
    private JsonNode stringData;
    
    @Schema(description = "Whether the secret is immutable", example = "false")
    private Boolean immutable;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
} 