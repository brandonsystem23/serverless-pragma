package com.pragma.domain.usecase;

import com.pragma.domain.api.IUserServicePort;
import com.pragma.domain.model.User;
import com.pragma.domain.spi.INotificationServicePort;
import com.pragma.domain.spi.IQueueServicePort;
import com.pragma.domain.spi.IUserPersistencePort;
import com.pragma.domain.validation.UserValidator;

import java.util.List;
import java.util.UUID;

public class UserUseCase implements IUserServicePort {

    private final IUserPersistencePort userPersistencePort;
    private final IQueueServicePort queueServicePort;
    private final INotificationServicePort notificationServicePort;

    public UserUseCase(IUserPersistencePort userPersistencePort,
                       IQueueServicePort queueServicePort,
                       INotificationServicePort notificationServicePort) {
        this.userPersistencePort = userPersistencePort;
        this.queueServicePort = queueServicePort;
        this.notificationServicePort = notificationServicePort;
    }

    @Override
    public User createUser(User user) {
        UserValidator.validateForCreate(user);
        user.setId(UUID.randomUUID().toString());
        User savedUser = userPersistencePort.save(user);
        queueServicePort.sendUserCreated(savedUser);
        return savedUser;
    }

    @Override
    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }

    @Override
    public User updateUser(String id, User user) {
        UserValidator.validateForUpdate(id, user);
        return userPersistencePort.update(id, user);
    }

    @Override
    public void deleteUser(String id) {
        UserValidator.validateId(id);
        userPersistencePort.delete(id);
    }

    @Override
    public void notifyUserCreated(User user) {
        UserValidator.validateForCreate(user);

        String formattedMessage = """
                ¡Hola!

                Se ha creado un usuario nuevo exitosamente.

                • Nombre: %s
                • Correo: %s
                • ID: %s

                ¡Bienvenido a la plataforma!
                """.formatted(user.getName(), user.getEmail(), user.getId());

        notificationServicePort.sendNotification("¡Notificación de nuevo usuario!", formattedMessage);
    }
}
