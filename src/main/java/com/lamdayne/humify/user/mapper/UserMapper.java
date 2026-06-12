package com.lamdayne.humify.user.mapper;

import com.lamdayne.humify.user.dto.request.CreateUserRequest;
import com.lamdayne.humify.user.dto.response.UserResponse;
import com.lamdayne.humify.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(CreateUserRequest request);

    UserResponse toResponse(User user);

}
