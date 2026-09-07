package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class HandlerGetTest {

    private final HandlerGet handler = new HandlerGet();

    @Test
    void shouldReturnUsersListSuccessfully() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Brandon Briones"));
        assertTrue(response.getBody().contains("Carlos Ruiz"));
        assertTrue(response.getBody().contains("Piero Arenas"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }
}
