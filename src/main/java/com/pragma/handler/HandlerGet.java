package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.model.User;
import com.pragma.util.DynamoDBClientProvider;
import com.pragma.util.ResponseUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.List;
import java.util.Map;

public class HandlerGet implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final DynamoDbClient dynamoDbClient = DynamoDBClientProvider.getClient();

    private static final String TABLE_NAME = System.getenv("TABLE_NAME") != null
                    ? System.getenv("TABLE_NAME") : "users";

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();

            ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

            List<User> users = scanResponse.items().stream()
                    .map(this::toUser)
                    .toList();

            return ResponseUtil.jsonResponse(200, users);

        } catch (Exception e) {
            context.getLogger().log(
                    "Error al consultar usuarios de DynamoDB: " + e.getMessage()
            );

            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }

    private User toUser(Map<String, AttributeValue> item) {
        return new User(
                getValue(item, "id"),
                getValue(item, "name"),
                getValue(item, "email")
        );
    }

    private String getValue(Map<String, AttributeValue> item, String key) {

        AttributeValue value = item.get(key);

        return value != null ? value.s() : null;
    }
}