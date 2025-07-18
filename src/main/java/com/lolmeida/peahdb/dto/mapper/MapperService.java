package com.lolmeida.peahdb.dto.mapper;

import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.request.UserPatchRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface MapperService {
    
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
}
