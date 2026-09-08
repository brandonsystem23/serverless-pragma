package com.pragma.domain.api;

import com.pragma.domain.model.User;

import java.util.List;

public interface IUserServicePort {
    User createUser(User user);
    List<User> getAllUsers();
    User updateUser(String id, User user);
    void deleteUser(String id);
    void notifyUserCreated(User user);
}
