package com.lolmeida.peahdb.dto.mapper;

import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;

// Core entities imports
import com.lolmeida.peahdb.dto.request.*;
import com.lolmeida.peahdb.dto.response.*;
import com.lolmeida.peahdb.entity.core.*;

import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface MapperService {
    
    // ========== USER MAPPERS ==========
    
    /**
     * Maps User entity to UserResponse DTO
     * Automatically excludes passwordHash as it's not in UserResponse
     */
    UserResponse toUserResponse(User user);
    
    /**
     * Maps UserRequest DTO to User entity
     * Excludes id, createdAt, updatedAt (managed by service)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserRequest userRequest);
    
    /**
     * Updates existing User entity with values from UserPatchRequest
     * Only updates non-null fields from the patch request
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromPatch(UserPatchRequest patchRequest, @MappingTarget User existingUser);
    
    /**
     * Maps UserRequest to User with specific ID (for PUT operations)
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "id", target = "id")
    User toUserWithId(UserRequest userRequest, Long id);
    
    // ========== CORE ENTITY MAPPERS ==========
    
    // Environment mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Environment toEnvironment(EnvironmentRequest environmentRequest);
    
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Environment toEnvironmentWithId(EnvironmentRequest environmentRequest, Long id);
    
    // Stack mappers
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "apps", ignore = true)
    @Mapping(source = "environmentId", target = "environment.id")
    Stack toStack(StackRequest stackRequest);
    
    @Mapping(source = "stackRequest.environmentId", target = "environment.id")
    @Mapping(source = "id", target = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "apps", ignore = true)
    Stack toStackWithId(StackRequest stackRequest, Long id);
    
    // ========== CORE ENTITY TO RESPONSE MAPPERS ==========
    
    // Environment response mappers
    EnvironmentResponse toEnvironmentResponse(Environment environment);
    
    // Stack response mappers
    @Mapping(source = "environment.id", target = "environmentId")
    StackResponse toStackResponse(Stack stack);
}
