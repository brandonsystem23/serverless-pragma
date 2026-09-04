package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.dto.MessageResponse;
import com.pragma.util.DynamoDBClientProvider;
import com.pragma.util.ResponseUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.HashMap;
import java.util.Map;

public class HandlerDelete implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

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

            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(key)
                    .build();

            dynamoDbClient.deleteItem(deleteItemRequest);

            return ResponseUtil.jsonResponse(200, new MessageResponse("Usuario con id " + id +
                    " eliminado correctamente de DynamoDB"));

        } catch (Exception e) {
            context.getLogger().log("Error al eliminar usuario de DynamoDB: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }
}