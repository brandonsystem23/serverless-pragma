package com.pragma.domain.spi;

import com.pragma.domain.model.User;

import java.util.List;

public interface IUserPersistencePort {
    User save(User user);
    List<User> findAll();
    User update(String id, User user);
    void delete(String id);
}
