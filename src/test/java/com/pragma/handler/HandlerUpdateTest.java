package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerUpdateTest {

    @Test
    void shouldCreateHandlerWithDefaultConstructor() {
        HandlerUpdate handler = new HandlerUpdate();
        assertNotNull(handler);
    }

    @Test
    void shouldReturn400WhenIdIsMissing() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{\"name\":\"Ana\",\"email\":\"ana@test.com\"}");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenIdIsBlank() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", ""));
        event.setBody("{\"name\":\"Ana\",\"email\":\"ana@test.com\"}");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenBodyIsBlank() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("   ");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void shouldReturn400WhenNoUpdatableFieldsAreProvided() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("{\"name\":\"   \",\"email\":\"   \"}");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Debe proporcionar al menos un campo"));
    }

    @Test
    void shouldReturn200WhenBodyIsValid() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

        UpdateItemResponse updateItemResponse = UpdateItemResponse.builder()
                .attributes(Map.of(
                        "id", AttributeValue.builder().s("999").build(),
                        "name", AttributeValue.builder().s("Ana Maria").build(),
                        "email", AttributeValue.builder().s("ana@example.com").build()
                ))
                .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenReturn(updateItemResponse);

        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("""
                {
                  "name": "Ana Maria",
                  "email": "ana@example.com"
                }
                """);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, mock(Context.class));

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario actualizado con éxito en DynamoDB"));
        verify(dynamoDbClient).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void shouldReturn500WhenBodyHasInvalidJson() {
        HandlerUpdate handler = new HandlerUpdate(mock(DynamoDbClient.class));

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("{invalid json}");

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        verify(logger).log(contains("Error al actualizar usuario de DynamoDB"));
    }

    @Test
    void shouldReturn500WhenDynamoFails() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(new RuntimeException("Dynamo error"));

        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("""
                {
                  "name": "Ana Maria",
                  "email": "ana@example.com"
                }
                """);

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Dynamo error"));
        verify(logger).log(contains("Error al actualizar usuario de DynamoDB"));
    }
}
