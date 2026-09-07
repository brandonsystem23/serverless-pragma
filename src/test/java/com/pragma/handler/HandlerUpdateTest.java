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
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setBody("{\"name\":\"Ana\",\"email\":\"ana@test.com\"}");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenIdIsBlank() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", ""));
        event.setBody("{\"name\":\"Ana\",\"email\":\"ana@test.com\"}");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El id es obligatorio en la ruta"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenBodyIsBlank() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("   ");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("El cuerpo de la solicitud es obligatorio"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn400WhenNoUpdatableFieldsAreProvided() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        event.setPathParameters(Map.of("id", "999"));
        event.setBody("{\"name\":\"   \",\"email\":\"   \"}");
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("Debe proporcionar al menos un campo"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
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

        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Usuario actualizado con éxito en DynamoDB"));
        assertTrue(response.getBody().contains("\"id\":\"999\""));
        assertTrue(response.getBody().contains("Ana Maria"));
        assertTrue(response.getBody().contains("ana@example.com"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(dynamoDbClient).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    void shouldReturn500WhenBodyHasInvalidJson() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        HandlerUpdate handler = new HandlerUpdate(dynamoDbClient);

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
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(logger).log(contains("Error al actualizar usuario de DynamoDB"));
    }

    @Test
    void shouldReturn500WhenDynamoDbFails() {
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
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        verify(logger).log(contains("Error al actualizar usuario de DynamoDB"));
    }
}
