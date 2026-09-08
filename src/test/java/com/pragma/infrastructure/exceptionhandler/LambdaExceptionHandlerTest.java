package com.pragma.infrastructure.exceptionhandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaExceptionHandlerTest {

    @Test
    void handleShouldReturn400ForValidationException() {
        APIGatewayV2HTTPResponse response = LambdaExceptionHandler.handle(
                new ValidationException("Dato inválido")
        );

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Dato inválido"));
    }

    @Test
    void handleShouldReturn500ForGenericException() {
        APIGatewayV2HTTPResponse response = LambdaExceptionHandler.handle(
                new RuntimeException("Error interno")
        );

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Error interno"));
    }
}
