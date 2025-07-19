package com.lolmeida.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "EnvironmentRequest",
    description = "Environment creation and update request data",
    example = """
    {
      "name": "development",
      "description": "Development environment for testing",
      "isActive": true
    }
    """
)
public class EnvironmentRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Name can only contain letters, numbers, underscores, and hyphens")
    @Schema(
        description = "Environment name (unique identifier)",
        example = "development",
        required = true,
        minLength = 2,
        maxLength = 100,
        pattern = "^[a-zA-Z0-9_-]+$"
    )
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(
        description = "Environment description",
        example = "Development environment for testing",
        maxLength = 500
    )
    private String description;
    
    @NotNull(message = "Active status is required")
    @Schema(
        description = "Whether the environment is active",
        example = "true",
        required = true
    )
    private Boolean isActive;
} 