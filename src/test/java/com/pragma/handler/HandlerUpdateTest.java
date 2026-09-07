package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandlerUpdateTest {

    private final HandlerUpdate handler = new HandlerUpdate();

    @Test
    void shouldReturn400WhenIdIsMissing() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{\"name\":\"Ana\",\"email\":\"ana@test.com\"}");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
    }

    @Test
    void shouldReturn200WhenIdAndBodyAreValid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("""
                {
                  "name": "Ana Maria",
                  "email": "ana@example.com"
                }
                """);
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario actualizado con éxito en DynamoDB"));
        assertTrue(response.getBody().contains("\"id\":\"999\""));
        assertTrue(response.getBody().contains("Ana Maria"));
        assertTrue(response.getBody().contains("ana@example.com"));
    }

    @Test
    void shouldReturn500WhenBodyHasInvalidJson() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("{invalid json}");

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        verify(logger).log(org.mockito.ArgumentMatchers.contains("Error al deserealizar"));
    }

}
