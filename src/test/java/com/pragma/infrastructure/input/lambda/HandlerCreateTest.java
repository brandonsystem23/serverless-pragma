package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.dto.request.UserRequest;
import com.pragma.application.dto.response.UserResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlerCreateTest {

    private IUserHandler userHandler;
    private HandlerCreate handler;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        userHandler = mock(IUserHandler.class);
        handler = new HandlerCreate(userHandler);
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void handleRequestShouldReturn201WhenRequestIsValid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{\"name\":\"Juan\",\"email\":\"juan@mail.com\"}");

        UserResponse userResponse = new UserResponse(
                "Usuario creado con éxito en DynamoDB y encolado en SQS",
                new User("1", "Juan", "juan@mail.com")
        );

        when(userHandler.createUser(any(UserRequest.class))).thenReturn(userResponse);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario creado con éxito"));
    }

    @Test
    void handleRequestShouldReturn400WhenBodyIsMissing() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody(" ");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
    }

    @Test
    void handleRequestShouldReturn500WhenJsonIsInvalid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{invalid-json}");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        verify(logger).log(contains("Error al crear usuario"));
    }
}
