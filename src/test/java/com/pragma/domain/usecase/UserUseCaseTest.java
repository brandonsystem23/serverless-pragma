package com.pragma.domain.usecase;

import com.pragma.domain.exception.ValidationException;
import com.pragma.domain.model.User;
import com.pragma.domain.spi.INotificationServicePort;
import com.pragma.domain.spi.IQueueServicePort;
import com.pragma.domain.spi.IUserPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserUseCaseTest {

    private IUserPersistencePort userPersistencePort;
    private IQueueServicePort queueServicePort;
    private INotificationServicePort notificationServicePort;
    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userPersistencePort = mock(IUserPersistencePort.class);
        queueServicePort = mock(IQueueServicePort.class);
        notificationServicePort = mock(INotificationServicePort.class);
        userUseCase = new UserUseCase(userPersistencePort, queueServicePort, notificationServicePort);
    }

    @Test
    void createUserShouldSaveUserAndSendToQueue() {
        User inputUser = new User(null, "Juan", "juan@mail.com");

        when(userPersistencePort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userUseCase.createUser(inputUser);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Juan", result.getName());
        assertEquals("juan@mail.com", result.getEmail());

        verify(userPersistencePort).save(any(User.class));
        verify(queueServicePort).sendUserCreated(result);
        verifyNoInteractions(notificationServicePort);
    }

    @Test
    void createUserShouldThrowWhenUserIsInvalid() {
        User inputUser = new User(null, "", "juan@mail.com");

        assertThrows(ValidationException.class, () -> userUseCase.createUser(inputUser));

        verifyNoInteractions(userPersistencePort, queueServicePort, notificationServicePort);
    }

    @Test
    void getAllUsersShouldReturnListFromPersistence() {
        List<User> users = List.of(
                new User("1", "Juan", "juan@mail.com"),
                new User("2", "Ana", "ana@mail.com")
        );

        when(userPersistencePort.findAll()).thenReturn(users);

        List<User> result = userUseCase.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(users, result);
        verify(userPersistencePort).findAll();
    }

    @Test
    void updateUserShouldValidateAndDelegateToPersistence() {
        User updateUser = new User(null, "Pedro", "pedro@mail.com");
        User updated = new User("123", "Pedro", "pedro@mail.com");

        when(userPersistencePort.update("123", updateUser)).thenReturn(updated);

        User result = userUseCase.updateUser("123", updateUser);

        assertEquals(updated, result);
        verify(userPersistencePort).update("123", updateUser);
    }

    @Test
    void updateUserShouldThrowWhenDataIsInvalid() {
        User updateUser = new User(null, "   ", "   ");

        assertThrows(ValidationException.class, () -> userUseCase.updateUser("123", updateUser));

        verify(userPersistencePort, never()).update(anyString(), any(User.class));
    }

    @Test
    void deleteUserShouldValidateAndDelegateToPersistence() {
        userUseCase.deleteUser("123");

        verify(userPersistencePort).delete("123");
    }

    @Test
    void deleteUserShouldThrowWhenIdIsInvalid() {
        assertThrows(ValidationException.class, () -> userUseCase.deleteUser(" "));

        verify(userPersistencePort, never()).delete(anyString());
    }

    @Test
    void notifyUserCreatedShouldSendNotification() {
        User user = new User("123", "Laura", "laura@mail.com");

        userUseCase.notifyUserCreated(user);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(notificationServicePort).sendNotification(subjectCaptor.capture(), messageCaptor.capture());

        assertEquals("¡Notificación de nuevo usuario!", subjectCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains("Laura"));
        assertTrue(messageCaptor.getValue().contains("laura@mail.com"));
        assertTrue(messageCaptor.getValue().contains("123"));
    }

    @Test
    void notifyUserCreatedShouldThrowWhenUserIsInvalid() {
        User user = new User(null, "", "");

        assertThrows(ValidationException.class, () -> userUseCase.notifyUserCreated(user));

        verifyNoInteractions(notificationServicePort);
    }
}
