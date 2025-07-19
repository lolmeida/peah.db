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
    name = "PersistentVolumeClaimRequest",
    description = "PersistentVolumeClaim creation and update request data",
    example = """
    {
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-pvc",
      "component": "database",
      "accessMode": "ReadWriteOnce",
      "size": "10Gi",
      "storageClass": "fast-ssd",
      "volumeMode": "Filesystem"
    }
    """
)
public class PersistentVolumeClaimRequest {
    
    @NotNull(message = "Service ID is required")
    @Schema(description = "Reference to service (App) ID", example = "1", required = true)
    private Long serviceId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the PVC is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Metadata name is required")
    @Size(max = 100, message = "Metadata name must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Metadata name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-pvc",
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
    
    @Size(max = 30, message = "Access mode must not exceed 30 characters")
    @Schema(
        description = "Access mode",
        example = "ReadWriteOnce",
        maxLength = 30,
        enumeration = {"ReadWriteOnce", "ReadOnlyMany", "ReadWriteMany"}
    )
    private String accessMode;
    
    @NotBlank(message = "Size is required")
    @Size(max = 20, message = "Size must not exceed 20 characters")
    @Pattern(regexp = "^\\d+[KMGTPE]i?$", message = "Size must be in format like '10Gi' or '500Mi'")
    @Schema(
        description = "Storage size (e.g., '10Gi', '500Mi')",
        example = "10Gi",
        required = true,
        maxLength = 20,
        pattern = "^\\d+[KMGTPE]i?$"
    )
    private String size;
    
    @Size(max = 100, message = "Storage class must not exceed 100 characters")
    @Schema(
        description = "Storage class name",
        example = "fast-ssd",
        maxLength = 100
    )
    private String storageClass;
    
    @Size(max = 20, message = "Volume mode must not exceed 20 characters")
    @Schema(
        description = "Volume mode",
        example = "Filesystem",
        maxLength = 20,
        enumeration = {"Filesystem", "Block"}
    )
    private String volumeMode;
    
    @Schema(description = "Additional access modes JSON array")
    private JsonNode accessModes;
    
    @Schema(description = "Resource requirements JSON")
    private JsonNode resources;
    
    @Schema(description = "Selector JSON")
    private JsonNode selector;
    
    @Size(max = 100, message = "Volume name must not exceed 100 characters")
    @Schema(
        description = "Specific volume name to bind to",
        example = "pv-database-01",
        maxLength = 100
    )
    private String volumeName;
    
    @Schema(description = "Data source JSON")
    private JsonNode dataSource;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
} 