package com.lolmeida.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.lolmeida.entity.k8s.AppManifest.ManifestType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "AppManifestRequest",
    description = "App manifest creation and update request data",
    example = """
    {
      "appId": 1,
      "manifestType": "DEPLOYMENT",
      "required": true,
      "creationPriority": 100,
      "description": "Main deployment manifest for the app"
    }
    """
)
public class AppManifestRequest {
    
    @NotNull(message = "App ID is required")
    @Schema(description = "Reference to app ID", example = "1", required = true)
    private Long appId;
    
    @NotNull(message = "Manifest type is required")
    @Schema(
        description = "Type of Kubernetes manifest",
        example = "DEPLOYMENT",
        required = true,
        enumeration = {"DEPLOYMENT", "SERVICE", "INGRESS", "PERSISTENT_VOLUME_CLAIM", "CONFIG_MAP", "SECRET", "SERVICE_ACCOUNT", "CLUSTER_ROLE", "HPA"}
    )
    private ManifestType manifestType;
    
    @NotNull(message = "Required flag is required")
    @Schema(description = "Whether this manifest is required", example = "true", required = true)
    private Boolean required;
    
    @Min(value = 1, message = "Creation priority must be at least 1")
    @Max(value = 1000, message = "Creation priority must not exceed 1000")
    @Schema(
        description = "Priority for creation (lower = create first)",
        example = "100",
        minimum = "1",
        maximum = "1000"
    )
    private Integer creationPriority;
    
    @Schema(description = "Default configuration JSON for this manifest")
    private JsonNode defaultConfig;
    
    @Schema(description = "Template overrides JSON")
    private JsonNode templateOverrides;
    
    @Size(max = 200, message = "Creation condition must not exceed 200 characters")
    @Schema(
        description = "Condition when this manifest should be created",
        example = "persistence.enabled",
        maxLength = 200
    )
    private String creationCondition;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
        description = "Description of why this manifest is needed",
        example = "Main deployment manifest for the app",
        maxLength = 500
    )
    private String description;
} 