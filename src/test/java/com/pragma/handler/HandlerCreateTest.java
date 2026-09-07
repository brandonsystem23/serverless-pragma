package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandlerCreateTest {

    private final HandlerCreate handler = new HandlerCreate();

    @Test
    void shouldReturn400WhenBodyIsNull() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
    }

    @Test
    void shouldReturn400WhenBodyIsBlank() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("   ");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
    }

    @Test
    void shouldReturn201WhenBodyIsValid() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {
                  "name": "Juan Perez",
                  "email": "juan@example.com"
                }
                """);
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario creado con éxito en DynamoDB y encolado en SQS"));
        assertTrue(response.getBody().contains("Juan Perez"));
        assertTrue(response.getBody().contains("juan@example.com"));
        assertTrue(response.getBody().contains("\"id\""));
    }

    @Test
    void shouldReturn500WhenBodyHasInvalidJson() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
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
