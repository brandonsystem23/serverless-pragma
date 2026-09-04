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


            StringBuilder updateExpression = new StringBuilder("SET ");

            Map<String, String> expressionAttributeNames = new HashMap<>();

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

            boolean hasUpdates = false;

            if (newDates.getName() != null && !newDates.getName().isBlank()) {
                updateExpression.append("#n = :name");
                expressionAttributeNames.put("#n", "name");
                expressionAttributeValues.put(":name", AttributeValue.builder().s(newDates.getName()).build());
                hasUpdates = true;
            }

            if (newDates.getEmail() != null && !newDates.getEmail().isBlank()) {
                if (hasUpdates) {
                    updateExpression.append(", ");
                }
                updateExpression.append("email = :email");
                expressionAttributeValues.put(":email", AttributeValue.builder().s(newDates.getEmail()).build());
                hasUpdates = true;
            }

            if (!hasUpdates) {
                return ResponseUtil.errorResponse(400, "Debe proporcionar al menos un campo ('name' o 'email') para actualizar.");
            }

            UpdateItemRequest.Builder updateRequestBuilder = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .updateExpression(updateExpression.toString())
                    .expressionAttributeValues(expressionAttributeValues);

            if (!expressionAttributeNames.isEmpty()) {
                updateRequestBuilder.expressionAttributeNames(expressionAttributeNames);
            }

            dynamoDbClient.updateItem(updateRequestBuilder.build());

            return ResponseUtil.jsonResponse(200, new MessageResponse("Usuario con id " + id +
                    " actualizado correctamente en DynamoDB"));

        } catch (Exception e) {
            context.getLogger().log("Error al actualizar usuario de DynamoDB: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }
}