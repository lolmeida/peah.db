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
    name = "UserRequest",
    description = "User creation and update request data",
    example = """
    {
      "username": "john_doe",
      "email": "john.doe@email.com",
      "passwordHash": "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
    }
    """
)
public class UserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(
        description = "Unique username for the user account",
        example = "john_doe",
        required = true,
        minLength = 3,
        maxLength = 50,
        pattern = "^[a-zA-Z0-9_]+$"
    )
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(
        description = "User's email address (must be unique and valid)",
        example = "john.doe@email.com",
        required = true,
        maxLength = 100,
        format = "email"
    )
    private String email;
    
    @NotBlank(message = "Password hash is required")
    @Size(min = 8, message = "Password hash must be at least 8 characters")
    @Schema(
        description = "Pre-hashed password (bcrypt recommended)",
        example = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
        required = true,
        minLength = 8
    )
    private String passwordHash;
} 