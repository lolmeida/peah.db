package com.lolmeida.peahdb.dto.response;

import java.time.LocalDateTime;

public record UserResponse (
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
} 