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
    name = "AppRequest",
    description = "App creation and update request data",
    example = """
    {
      "name": "postgresql",
      "displayName": "PostgreSQL Database",
      "description": "PostgreSQL database server",
      "category": "database",
      "version": "15.0",
      "defaultImageRepository": "postgres",
      "defaultImageTag": "15",
      "enabled": true
    }
    """
)
public class AppRequest {
    
    @NotNull(message = "Stack ID is required")
    @Schema(description = "Reference to stack ID", example = "1", required = true)
    private Long stackId;
    
    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the app is enabled", example = "true", required = true)
    private Boolean enabled;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Name can only contain lowercase letters, numbers, and hyphens")
    @Schema(
        description = "Unique name for the app (lowercase with hyphens)",
        example = "postgresql",
        required = true,
        minLength = 2,
        maxLength = 100,
        pattern = "^[a-z0-9-]+$"
    )
    private String name;
    
    @NotBlank(message = "Display name is required")
    @Size(max = 200, message = "Display name must not exceed 200 characters")
    @Schema(
        description = "Human-readable display name",
        example = "PostgreSQL Database",
        required = true,
        maxLength = 200
    )
    private String displayName;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
        description = "App description",
        example = "PostgreSQL database server",
        maxLength = 500
    )
    private String description;
    
    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Schema(
        description = "App category",
        example = "database",
        required = true,
        maxLength = 50
    )
    private String category;
    
    @Size(max = 20, message = "Version must not exceed 20 characters")
    @Schema(
        description = "App version",
        example = "15.0",
        maxLength = 20
    )
    private String version;
    
    @Size(max = 200, message = "Default image repository must not exceed 200 characters")
    @Schema(
        description = "Default Docker image repository",
        example = "postgres",
        maxLength = 200
    )
    private String defaultImageRepository;
    
    @Size(max = 50, message = "Default image tag must not exceed 50 characters")
    @Schema(
        description = "Default Docker image tag",
        example = "15",
        maxLength = 50
    )
    private String defaultImageTag;
    
    @Schema(description = "Default configuration JSON")
    private JsonNode defaultConfig;
    
    @Schema(description = "App dependencies JSON array")
    private JsonNode dependencies;
    
    @Schema(description = "Default ports configuration JSON")
    private JsonNode defaultPorts;
    
    @Schema(description = "Default resource requirements JSON")
    private JsonNode defaultResources;
    
    @Size(max = 100, message = "Health check path must not exceed 100 characters")
    @Schema(
        description = "Health check endpoint path",
        example = "/health",
        maxLength = 100
    )
    private String healthCheckPath;
    
    @Size(max = 100, message = "Readiness check path must not exceed 100 characters")
    @Schema(
        description = "Readiness check endpoint path",
        example = "/ready",
        maxLength = 100
    )
    private String readinessCheckPath;
    
    @Size(max = 300, message = "Documentation URL must not exceed 300 characters")
    @Schema(
        description = "Documentation URL",
        example = "https://www.postgresql.org/docs/",
        maxLength = 300
    )
    private String documentationUrl;
    
    @Size(max = 300, message = "Icon URL must not exceed 300 characters")
    @Schema(
        description = "Icon URL",
        example = "https://example.com/postgresql-icon.png",
        maxLength = 300
    )
    private String iconUrl;
    
    @Min(value = 1, message = "Deployment priority must be at least 1")
    @Max(value = 1000, message = "Deployment priority must not exceed 1000")
    @Schema(
        description = "Deployment priority (lower = deploy first)",
        example = "100",
        minimum = "1",
        maximum = "1000"
    )
    private Integer deploymentPriority;
} 