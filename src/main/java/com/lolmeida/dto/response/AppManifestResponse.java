package com.lolmeida.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.lolmeida.entity.k8s.AppManifest.ManifestType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "AppManifestResponse",
    description = "App manifest response data",
    example = """
    {
      "id": 1,
      "appId": 1,
      "manifestType": "DEPLOYMENT",
      "required": true,
      "creationPriority": 100,
      "description": "Main deployment manifest for the app",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class AppManifestResponse {
    
    @Schema(
        description = "Unique identifier for the app manifest",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "Reference to app ID",
        example = "1"
    )
    private Long appId;
    
    @Schema(
        description = "Type of Kubernetes manifest",
        example = "DEPLOYMENT"
    )
    private ManifestType manifestType;
    
    @Schema(
        description = "Whether this manifest is required",
        example = "true"
    )
    private Boolean required;
    
    @Schema(
        description = "Priority for creation (lower = create first)",
        example = "100"
    )
    private Integer creationPriority;
    
    @Schema(description = "Default configuration JSON for this manifest")
    private JsonNode defaultConfig;
    
    @Schema(description = "Template overrides JSON")
    private JsonNode templateOverrides;
    
    @Schema(
        description = "Condition when this manifest should be created",
        example = "persistence.enabled"
    )
    private String creationCondition;
    
    @Schema(
        description = "Description of why this manifest is needed",
        example = "Main deployment manifest for the app"
    )
    private String description;
    
    @Schema(
        description = "Timestamp when the app manifest was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the app manifest was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 