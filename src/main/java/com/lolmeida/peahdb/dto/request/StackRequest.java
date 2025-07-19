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
    name = "StackRequest",
    description = "Stack creation and update request data",
    example = """
    {
      "environmentId": 1,
      "name": "web-stack",
      "enabled": true,
      "description": "Web application stack with database and cache",
      "config": {
        "replicas": 3,
        "resources": {
          "cpu": "500m",
          "memory": "1Gi"
        }
      }
    }
    """
)
public class StackRequest {
    
    @NotNull(message = "Environment ID is required")
    @Schema(description = "Reference to environment ID", example = "1", required = true)
    private Long environmentId;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Name can only contain letters, numbers, underscores, and hyphens")
    @Schema(
        description = "Stack name (unique identifier within environment)",
        example = "web-stack",
        required = true,
        minLength = 2,
        maxLength = 100,
        pattern = "^[a-zA-Z0-9_-]+$"
    )
    private String name;
    
    @NotNull(message = "Enabled status is required")
    @Schema(
        description = "Whether the stack is enabled",
        example = "true",
        required = true
    )
    private Boolean enabled;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
        description = "Stack description",
        example = "Web application stack with database and cache",
        maxLength = 500
    )
    private String description;
    
    @Schema(
        description = "Stack configuration as JSON",
        example = """
        {
          "replicas": 3,
          "resources": {
            "cpu": "500m",
            "memory": "1Gi"
          }
        }
        """
    )
    private JsonNode config;
} 