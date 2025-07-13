package com.lolmeida.peahdb.dto.request;

import java.time.LocalDateTime;

public record UserRequest (
     Long id,
     String username,
     String email,
     LocalDateTime createdAt,
     LocalDateTime updatedAt
){
} 