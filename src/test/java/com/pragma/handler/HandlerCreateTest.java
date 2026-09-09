package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerCreateTest {

    @Test
    void shouldCreateHandlerWithDefaultConstructor() {
        HandlerCreate handler = new HandlerCreate();
        assertNotNull(handler);
    }


    @Test
    void shouldReturn400WhenBodyIsNull() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerCreate handler = new HandlerCreate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenBodyIsBlank() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerCreate handler = new HandlerCreate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("   ");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn201WhenBodyIsValid() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerCreate handler = new HandlerCreate(dynamoDbClient);

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
        assertTrue(response.getBody().contains("Usuario creado con éxito en DynamoDB"));
        assertTrue(response.getBody().contains("Juan Perez"));
        assertTrue(response.getBody().contains("juan@example.com"));
        assertTrue(response.getBody().contains("\"id\""));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void shouldReturn500WhenBodyHasInvalidJson() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerCreate handler = new HandlerCreate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{invalid json}");

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(logger).log(contains("Error al crear usuario"));
    }

    @Test
    void shouldReturn500WhenDynamoDbFails() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

        doThrow(new RuntimeException("Dynamo error"))
                .when(dynamoDbClient)
                .putItem(any(PutItemRequest.class));

        HandlerCreate handler = new HandlerCreate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("""
                {
                  "name": "Juan Perez",
                  "email": "juan@example.com"
                }
                """);

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Dynamo error"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(logger).log(contains("Error al crear usuario"));
    }
}
