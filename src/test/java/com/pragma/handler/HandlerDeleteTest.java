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
        HandlerDelete handler = new HandlerDelete(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenIdIsBlank() {
        HandlerDelete handler = new HandlerDelete(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", ""));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn200WhenDeleteIsSuccessful() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerDelete handler = new HandlerDelete(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "123"));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario con id 123 eliminado correctamente de DynamoDB"));
        verify(dynamoDbClient).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    void shouldReturn500WhenDynamoFails() {
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
        verify(logger).log(contains("Error al eliminar usuario de DynamoDB"));
    }
}
