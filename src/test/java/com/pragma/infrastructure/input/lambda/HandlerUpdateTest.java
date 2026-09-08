package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.dto.response.UserResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerUpdateTest {

    private IUserHandler userHandler;
    private HandlerUpdate handler;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        userHandler = mock(IUserHandler.class);
        handler = new HandlerUpdate(userHandler);
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void handleRequestShouldReturn200WhenRequestIsValid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));
        event.setBody("{\"name\":\"Pedro\",\"email\":\"pedro@mail.com\"}");

        when(userHandler.updateUser(eq("123"), any())).thenReturn(
                new UserResponse("Usuario actualizado con éxito en DynamoDB",
                        new User("123", "Pedro", "pedro@mail.com"))
        );

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario actualizado con éxito"));
    }

    @Test
    void handleRequestShouldReturn400WhenBodyIsMissing() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));
        event.setBody(" ");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
    }

    @Test
    void handleRequestShouldReturn500WhenJsonIsInvalid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));
        event.setBody("{bad-json}");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        verify(logger).log(contains("Error al actualizar usuario"));
    }
}
