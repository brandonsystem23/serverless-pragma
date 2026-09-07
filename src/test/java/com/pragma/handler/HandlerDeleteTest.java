package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlerDeleteTest {

    private final HandlerDelete handler = new HandlerDelete();

    @Test
    void shouldReturn400WhenIdIsMissing() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
    }

    @Test
    void shouldReturn400WhenIdIsBlank() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", ""));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
    }

    @Test
    void shouldReturn200WhenIdIsPresent() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario con id 123 eliminado correctamente de DynamoDB"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }
}
