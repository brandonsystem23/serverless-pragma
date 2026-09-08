package com.pragma.domain.validation;

import com.pragma.domain.exception.ValidationException;
import com.pragma.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    @Test
    void validateForCreateShouldThrowWhenUserIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForCreate(null)
        );

        assertEquals("El usuario es obligatorio", exception.getMessage());
    }

    @Test
    void validateForCreateShouldThrowWhenNameIsNull() {
        User user = new User(null, null, "test@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForCreate(user)
        );

        assertEquals("El nombre es obligatorio", exception.getMessage());
    }

    @Test
    void validateForCreateShouldThrowWhenNameIsBlank() {
        User user = new User(null, "   ", "test@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForCreate(user)
        );

        assertEquals("El nombre es obligatorio", exception.getMessage());
    }

    @Test
    void validateForCreateShouldThrowWhenEmailIsNull() {
        User user = new User(null, "Juan", null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForCreate(user)
        );

        assertEquals("El correo es obligatorio", exception.getMessage());
    }

    @Test
    void validateForCreateShouldThrowWhenEmailIsBlank() {
        User user = new User(null, "Juan", "   ");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForCreate(user)
        );

        assertEquals("El correo es obligatorio", exception.getMessage());
    }

    @Test
    void validateForCreateShouldPassWhenUserIsValid() {
        User user = new User(null, "Juan", "juan@mail.com");

        assertDoesNotThrow(() -> UserValidator.validateForCreate(user));
    }

    @Test
    void validateForUpdateShouldThrowWhenIdIsNull() {
        User user = new User(null, "Juan", "juan@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForUpdate(null, user)
        );

        assertEquals("El id es obligatorio en la ruta", exception.getMessage());
    }

    @Test
    void validateForUpdateShouldThrowWhenIdIsBlank() {
        User user = new User(null, "Juan", "juan@mail.com");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForUpdate("   ", user)
        );

        assertEquals("El id es obligatorio en la ruta", exception.getMessage());
    }

    @Test
    void validateForUpdateShouldThrowWhenUserIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForUpdate("123", null)
        );

        assertEquals("El usuario es obligatorio", exception.getMessage());
    }

    @Test
    void validateForUpdateShouldThrowWhenNoFieldsAreProvided() {
        User user = new User(null, "   ", "   ");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateForUpdate("123", user)
        );

        assertEquals("Debe proporcionar al menos un campo ('name' o 'email') para actualizar.", exception.getMessage());
    }

    @Test
    void validateForUpdateShouldPassWhenNameIsProvided() {
        User user = new User(null, "Nuevo nombre", null);

        assertDoesNotThrow(() -> UserValidator.validateForUpdate("123", user));
    }

    @Test
    void validateForUpdateShouldPassWhenEmailIsProvided() {
        User user = new User(null, null, "nuevo@mail.com");

        assertDoesNotThrow(() -> UserValidator.validateForUpdate("123", user));
    }

    @Test
    void validateIdShouldThrowWhenIdIsNull() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateId(null)
        );

        assertEquals("El id es obligatorio en la ruta", exception.getMessage());
    }

    @Test
    void validateIdShouldThrowWhenIdIsBlank() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> UserValidator.validateId("   ")
        );

        assertEquals("El id es obligatorio en la ruta", exception.getMessage());
    }

    @Test
    void validateIdShouldPassWhenIdIsValid() {
        assertDoesNotThrow(() -> UserValidator.validateId("123"));
    }
}
