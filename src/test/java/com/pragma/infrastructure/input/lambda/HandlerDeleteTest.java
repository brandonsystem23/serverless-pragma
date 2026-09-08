package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.handler.IUserHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlerDeleteTest {

    private IUserHandler userHandler;
    private HandlerDelete handler;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        userHandler = mock(IUserHandler.class);
        handler = new HandlerDelete(userHandler);
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void handleRequestShouldReturn200WhenDeleteIsSuccessful() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario con id 123 eliminado correctamente de DynamoDB"));
        verify(userHandler).deleteUser("123");
    }

    @Test
    void handleRequestShouldReturn500WhenExceptionOccurs() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));

        doThrow(new RuntimeException("error")).when(userHandler).deleteUser("123");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        verify(logger).log(contains("Error al eliminar usuario"));
    }
}
