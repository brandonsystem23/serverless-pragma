package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerGetTest {

    @Test
    void shouldCreateHandlerWithDefaultConstructor() {
        HandlerGet handler = new HandlerGet();
        assertNotNull(handler);
    }

    @Test
    void shouldReturn200WhenUsersAreRetrievedSuccessfully() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

        ScanResponse scanResponse = ScanResponse.builder()
                .items(List.of(
                        Map.of(
                                "id", AttributeValue.builder().s("1").build(),
                                "name", AttributeValue.builder().s("Juan").build(),
                                "email", AttributeValue.builder().s("juan@test.com").build()
                        ),
                        Map.of(
                                "id", AttributeValue.builder().s("2").build(),
                                "name", AttributeValue.builder().s("Ana").build(),
                                "email", AttributeValue.builder().s("ana@test.com").build()
                        )
                ))
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

        HandlerGet handler = new HandlerGet(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("Juan"));
        assertTrue(response.getBody().contains("Ana"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn200WhenTableIsEmpty() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);

        ScanResponse scanResponse = ScanResponse.builder()
                .items(List.of())
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

        HandlerGet handler = new HandlerGet(dynamoDbClient);

        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        Context context = mock(Context.class);

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(200, response.getStatusCode());
        assertEquals("[]", response.getBody());
    }

    @Test
    void shouldReturn500WhenDynamoFails() {
        DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
        when(dynamoDbClient.scan(any(ScanRequest.class)))
                .thenThrow(new RuntimeException("Dynamo error"));

        HandlerGet handler = new HandlerGet(dynamoDbClient);

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        APIGatewayV2HTTPResponse response = handler.handleRequest(new APIGatewayV2HTTPEvent(), context);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Dynamo error"));
        verify(logger).log(contains("Error al consultar usuarios de DynamoDB"));
    }
}
