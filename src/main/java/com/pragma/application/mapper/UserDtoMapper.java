package com.pragma.application.mapper;

import com.pragma.application.dto.request.UserRequest;
import com.pragma.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {
    User toUser(UserRequest request);
}
