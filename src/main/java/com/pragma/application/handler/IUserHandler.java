package com.pragma.application.handler;

import com.pragma.application.dto.request.UserRequest;
import com.pragma.application.dto.response.UserResponse;
import com.pragma.domain.model.User;

import java.util.List;

public interface IUserHandler {
    UserResponse createUser(UserRequest request);
    List<User> getAllUsers();
    UserResponse updateUser(String id, UserRequest request);
    void deleteUser(String id);
    void notifyUserCreated(User user);
}
