package com.lolmeida.peahdb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "EnvironmentResponse",
    description = "Environment response data",
    example = """
    {
      "id": 1,
      "name": "development",
      "description": "Development environment for testing",
      "isActive": true,
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class EnvironmentResponse {
    
    @Schema(
        description = "Unique identifier for the environment",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "Environment name",
        example = "development"
    )
    private String name;
    
    @Schema(
        description = "Environment description",
        example = "Development environment for testing"
    )
    private String description;
    
    @Schema(
        description = "Whether the environment is active",
        example = "true"
    )
    private Boolean isActive;
    
    @Schema(
        description = "Timestamp when the environment was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the environment was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 