package com.lolmeida.peahdb.dto.mapper;


import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;
import org.mapstruct.*;


@Mapper(
        componentModel = MappingConstants.ComponentModel.JAKARTA_CDI,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)

public interface MapperService {
    UserResponse toUserResponse(User entity);

    @Mapping(target = "passwordHash", ignore = true)
    User toUser(UserRequest request);
}
