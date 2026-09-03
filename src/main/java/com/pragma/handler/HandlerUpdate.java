package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.dto.MessageResponse;
import com.pragma.model.User;
import com.pragma.util.DynamoDBClientProvider;
import com.pragma.util.ResponseUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.Map;

public class HandlerUpdate implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DynamoDbClient dynamoDbClient = DynamoDBClientProvider.getClient();
    private static final String TABLE_NAME = System.getenv("TABLE_NAME") != null ?
            System.getenv("TABLE_NAME") : "users";

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String id = (event.getPathParameters() != null) ? event.getPathParameters().get("id") : null;

            if (id == null || id.isBlank()) {
                return ResponseUtil.errorResponse(400, "El id es obligatorio en la ruta");
            }

            if (event.getBody() == null || event.getBody().isBlank()) {
                return ResponseUtil.errorResponse(400, "El cuerpo de la solicitud es obligatorio");
            }

            User newDates = MAPPER.readValue(event.getBody(), User.class);

            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());


            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":name", AttributeValue.builder().s(newDates.getName()).build());
            expressionAttributeValues.put(":email", AttributeValue.builder().s(newDates.getEmail()).build());

            UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .updateExpression("SET #n = :name, email = :email")
                    .expressionAttributeNames(Map.of("#n", "name"))
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            dynamoDbClient.updateItem(updateItemRequest);

            return ResponseUtil.jsonResponse(200, new MessageResponse("Usuario con id " + id +
                    " actualizado correctamente en DynamoDB"));

        } catch (Exception e) {
            context.getLogger().log("Error al actualizar en DynamoDB: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }
}