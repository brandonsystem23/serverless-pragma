package com.pragma.domain.validation;

import com.pragma.domain.exception.ValidationException;
import com.pragma.domain.model.User;

public class UserValidator {

    private UserValidator() {
    }

    public static void validateForCreate(User user) {
        if (user == null) {
            throw new ValidationException("El usuario es obligatorio");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("El nombre es obligatorio");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("El correo es obligatorio");
        }
    }

    public static void validateForUpdate(String id, User user) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("El id es obligatorio en la ruta");
        }
        if (user == null) {
            throw new ValidationException("El usuario es obligatorio");
        }
        boolean hasName = user.getName() != null && !user.getName().isBlank();
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();

        if (!hasName && !hasEmail) {
            throw new ValidationException("Debe proporcionar al menos un campo ('name' o 'email') para actualizar.");
        }
    }

    public static void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException("El id es obligatorio en la ruta");
        }
    }
}
