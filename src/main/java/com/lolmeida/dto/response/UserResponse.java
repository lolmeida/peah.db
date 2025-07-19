package com.lolmeida.dto.response;

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
    name = "UserResponse",
    description = "User response data (excludes sensitive information like password)",
    example = """
    {
      "id": 1,
      "username": "john_doe",
      "email": "john.doe@email.com",
      "createdAt": "2023-01-15T10:30:00",
      "updatedAt": "2023-01-15T10:30:00"
    }
    """
)
public class UserResponse {
    
    @Schema(
        description = "Unique identifier for the user",
        example = "1",
        readOnly = true
    )
    private Long id;
    
    @Schema(
        description = "User's unique username",
        example = "john_doe",
        minLength = 3,
        maxLength = 50
    )
    private String username;
    
    @Schema(
        description = "User's email address",
        example = "john.doe@email.com",
        format = "email",
        maxLength = 100
    )
    private String email;
    
    @Schema(
        description = "Timestamp when the user was created",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime createdAt;
    
    @Schema(
        description = "Timestamp when the user was last updated",
        example = "2023-01-15T10:30:00",
        readOnly = true
    )
    private LocalDateTime updatedAt;
} 