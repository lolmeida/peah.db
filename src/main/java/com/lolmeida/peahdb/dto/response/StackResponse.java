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
    name = "StackResponse",
    description = "Stack response data",
    example = """
    {
      "id": 1,
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
      },
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class StackResponse {
    
    @Schema(
        description = "Unique identifier for the stack",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "Reference to environment ID",
        example = "1"
    )
    private Long environmentId;
    
    @Schema(
        description = "Stack name",
        example = "web-stack"
    )
    private String name;
    
    @Schema(
        description = "Whether the stack is enabled",
        example = "true"
    )
    private Boolean enabled;
    
    @Schema(
        description = "Stack description",
        example = "Web application stack with database and cache"
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
    
    @Schema(
        description = "Timestamp when the stack was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the stack was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 