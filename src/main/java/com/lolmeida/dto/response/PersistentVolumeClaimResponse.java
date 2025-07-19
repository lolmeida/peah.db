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
    name = "PersistentVolumeClaimResponse",
    description = "PersistentVolumeClaim response data",
    example = """
    {
      "id": 1,
      "serviceId": 1,
      "enabled": true,
      "metadataName": "postgresql-pvc",
      "component": "database",
      "accessMode": "ReadWriteOnce",
      "size": "10Gi",
      "storageClass": "fast-ssd",
      "volumeMode": "Filesystem",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class PersistentVolumeClaimResponse {
    
    @Schema(
        description = "Unique identifier for the PVC",
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
        description = "Whether the PVC is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Kubernetes metadata name",
        example = "postgresql-pvc"
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
        description = "Access mode",
        example = "ReadWriteOnce"
    )
    private String accessMode;
    
    @Schema(
        description = "Storage size",
        example = "10Gi"
    )
    private String size;
    
    @Schema(
        description = "Storage class name",
        example = "fast-ssd"
    )
    private String storageClass;
    
    @Schema(
        description = "Volume mode",
        example = "Filesystem"
    )
    private String volumeMode;
    
    @Schema(description = "Additional access modes JSON array")
    private JsonNode accessModes;
    
    @Schema(description = "Resource requirements JSON")
    private JsonNode resources;
    
    @Schema(description = "Selector JSON")
    private JsonNode selector;
    
    @Schema(
        description = "Specific volume name to bind to",
        example = "pv-database-01"
    )
    private String volumeName;
    
    @Schema(description = "Data source JSON")
    private JsonNode dataSource;
    
    @Schema(description = "Annotations JSON")
    private JsonNode annotations;
    
    @Schema(
        description = "Timestamp when the PVC was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the PVC was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 