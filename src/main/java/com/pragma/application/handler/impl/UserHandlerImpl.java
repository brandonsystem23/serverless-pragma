package com.pragma.application.handler.impl;

import com.pragma.application.dto.request.UserRequest;
import com.pragma.application.dto.response.UserResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.application.mapper.UserDtoMapper;
import com.pragma.domain.api.IUserServicePort;
import com.pragma.domain.model.User;

import java.util.List;

public class UserHandlerImpl implements IUserHandler {

    private final IUserServicePort userServicePort;
    private final UserDtoMapper userDtoMapper;

    public UserHandlerImpl(IUserServicePort userServicePort, UserDtoMapper userDtoMapper) {
        this.userServicePort = userServicePort;
        this.userDtoMapper = userDtoMapper;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        User user = userDtoMapper.toUser(request);
        User createdUser = userServicePort.createUser(user);
        return new UserResponse("Usuario creado con éxito en DynamoDB y encolado en SQS", createdUser);
    }

    @Override
    public List<User> getAllUsers() {
        return userServicePort.getAllUsers();
    }

    @Override
    public UserResponse updateUser(String id, UserRequest request) {
        User user = userDtoMapper.toUser(request);
        User updatedUser = userServicePort.updateUser(id, user);
        return new UserResponse("Usuario actualizado con éxito en DynamoDB", updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        userServicePort.deleteUser(id);
    }

    @Override
    public void notifyUserCreated(User user) {
        userServicePort.notifyUserCreated(user);
    }
}
