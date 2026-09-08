package com.pragma.application.handler.impl;

import com.pragma.application.dto.request.UserRequest;
import com.pragma.application.dto.response.UserResponse;
import com.pragma.application.mapper.UserDtoMapper;
import com.pragma.domain.api.IUserServicePort;
import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserHandlerImplTest {

    private IUserServicePort userServicePort;
    private UserDtoMapper userDtoMapper;
    private UserHandlerImpl userHandler;

    @BeforeEach
    void setUp() {
        userServicePort = mock(IUserServicePort.class);
        userDtoMapper = mock(UserDtoMapper.class);
        userHandler = new UserHandlerImpl(userServicePort, userDtoMapper);
    }

    @Test
    void createUserShouldMapAndDelegate() {
        UserRequest request = new UserRequest("Juan", "juan@mail.com");
        User mappedUser = new User(null, "Juan", "juan@mail.com");
        User createdUser = new User("123", "Juan", "juan@mail.com");

        when(userDtoMapper.toUser(request)).thenReturn(mappedUser);
        when(userServicePort.createUser(mappedUser)).thenReturn(createdUser);

        UserResponse response = userHandler.createUser(request);

        assertNotNull(response);
        assertEquals("Usuario creado con éxito en DynamoDB y encolado en SQS", response.getMessage());
        assertEquals(createdUser, response.getUser());

        verify(userDtoMapper).toUser(request);
        verify(userServicePort).createUser(mappedUser);
    }

    @Test
    void getAllUsersShouldDelegate() {
        List<User> users = List.of(new User("1", "Ana", "ana@mail.com"));

        when(userServicePort.getAllUsers()).thenReturn(users);

        List<User> result = userHandler.getAllUsers();

        assertEquals(users, result);
        verify(userServicePort).getAllUsers();
    }

    @Test
    void updateUserShouldMapAndDelegate() {
        UserRequest request = new UserRequest("Pedro", "pedro@mail.com");
        User mappedUser = new User(null, "Pedro", "pedro@mail.com");
        User updatedUser = new User("999", "Pedro", "pedro@mail.com");

        when(userDtoMapper.toUser(request)).thenReturn(mappedUser);
        when(userServicePort.updateUser("999", mappedUser)).thenReturn(updatedUser);

        UserResponse response = userHandler.updateUser("999", request);

        assertNotNull(response);
        assertEquals("Usuario actualizado con éxito en DynamoDB", response.getMessage());
        assertEquals(updatedUser, response.getUser());

        verify(userDtoMapper).toUser(request);
        verify(userServicePort).updateUser("999", mappedUser);
    }

    @Test
    void deleteUserShouldDelegate() {
        userHandler.deleteUser("123");

        verify(userServicePort).deleteUser("123");
    }

    @Test
    void notifyUserCreatedShouldDelegate() {
        User user = new User("1", "Maria", "maria@mail.com");

        userHandler.notifyUserCreated(user);

        verify(userServicePort).notifyUserCreated(user);
    }
}
