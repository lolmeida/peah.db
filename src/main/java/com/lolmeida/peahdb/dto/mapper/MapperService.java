package com.lolmeida.peahdb.dto.mapper;


import com.lolmeida.peahdb.dto.request.UserRequest;
import com.lolmeida.peahdb.dto.response.UserResponse;
import com.lolmeida.peahdb.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(
        componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapperService {
    UserResponse toUserResponse(User entity);
    User toUser(UserRequest request);
}
