package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlerGetTest {

    private IUserHandler userHandler;
    private HandlerGet handler;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        userHandler = mock(IUserHandler.class);
        handler = new HandlerGet(userHandler);
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void handleRequestShouldReturn200WhenSuccess() {
        when(userHandler.getAllUsers()).thenReturn(List.of(
                new User("1", "Juan", "juan@mail.com")
        ));

        APIGatewayV2HTTPResponse response = handler.handleRequest(new APIGatewayV2HTTPEvent(), context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Juan"));
    }

    @Test
    void handleRequestShouldReturn500WhenExceptionOccurs() {
        when(userHandler.getAllUsers()).thenThrow(new RuntimeException("falló"));

        APIGatewayV2HTTPResponse response = handler.handleRequest(new APIGatewayV2HTTPEvent(), context);

        assertEquals(500, response.getStatusCode());
        verify(logger).log(contains("Error al consultar usuarios"));
    }
}
