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
    name = "AppResponse",
    description = "App response data",
    example = """
    {
      "id": 1,
      "stackId": 1,
      "enabled": true,
      "name": "postgresql",
      "displayName": "PostgreSQL Database",
      "description": "PostgreSQL database server",
      "category": "database",
      "version": "15.0",
      "defaultImageRepository": "postgres",
      "defaultImageTag": "15",
      "deploymentPriority": 50,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class AppResponse {
    
    @Schema(
        description = "Unique identifier for the app",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "Reference to stack ID",
        example = "1"
    )
    private Long stackId;
    
    @Schema(
        description = "Whether the app is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Unique name for the app",
        example = "postgresql"
    )
    private String name;
    
    @Schema(
        description = "Human-readable display name",
        example = "PostgreSQL Database"
    )
    private String displayName;
    
    @Schema(
        description = "App description",
        example = "PostgreSQL database server"
    )
    private String description;
    
    @Schema(
        description = "App category",
        example = "database"
    )
    private String category;
    
    @Schema(
        description = "App version",
        example = "15.0"
    )
    private String version;
    
    @Schema(
        description = "Default Docker image repository",
        example = "postgres"
    )
    private String defaultImageRepository;
    
    @Schema(
        description = "Default Docker image tag",
        example = "15"
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
    
    @Schema(
        description = "Health check endpoint path",
        example = "/health"
    )
    private String healthCheckPath;
    
    @Schema(
        description = "Readiness check endpoint path",
        example = "/ready"
    )
    private String readinessCheckPath;
    
    @Schema(
        description = "Documentation URL",
        example = "https://www.postgresql.org/docs/"
    )
    private String documentationUrl;
    
    @Schema(
        description = "Icon URL",
        example = "https://example.com/postgresql-icon.png"
    )
    private String iconUrl;
    
    @Schema(
        description = "Deployment priority (lower = deploy first)",
        example = "50"
    )
    private Integer deploymentPriority;
    
    @Schema(
        description = "Timestamp when the app was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the app was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 