package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerDeleteTest {

    @Test
    void shouldCreateHandlerWithDefaultConstructor() {
        HandlerDelete handler = new HandlerDelete();
        assertNotNull(handler);
    }


    @Test
    void shouldReturn400WhenIdIsMissing() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerDelete handler = new HandlerDelete(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenIdIsBlank() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerDelete handler = new HandlerDelete(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", ""));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn200WhenDeleteIsSuccessful() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerDelete handler = new HandlerDelete(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario con id 123 eliminado correctamente de DynamoDB"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(dynamoDbClient).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    void shouldReturn500WhenDynamoDbFails() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

        doThrow(new RuntimeException("Dynamo error"))
                .when(dynamoDbClient)
                .deleteItem(any(DeleteItemRequest.class));

        HandlerDelete handler = new HandlerDelete(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Dynamo error"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(logger).log(contains("Error al eliminar usuario de DynamoDB"));
    }
}
