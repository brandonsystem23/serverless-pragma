package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.dto.UserResponse;
import com.pragma.model.User;
import com.pragma.util.DynamoDBClientProvider;
import com.pragma.util.SqsClientProvider;
import com.pragma.util.ResponseUtil;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HandlerCreate implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DynamoDbClient dynamoDbClient = DynamoDBClientProvider.getClient();
    private static final SqsClient sqsClient = SqsClientProvider.getClient();

    private static final String QUEUE_URL = System.getenv("QUEUE_URL") != null ?
            System.getenv("QUEUE_URL") : "https://sqs.us-east-1.amazonaws.com/121604171970/cola-pragma";

    private static final String TABLE_NAME = System.getenv("TABLE_NAME") != null ?
            System.getenv("TABLE_NAME") : "users";

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            if (event.getBody() == null || event.getBody().isBlank()) {
                return ResponseUtil.errorResponse(400, "El cuerpo de la solicitud es obligatorio");
            }

            User nuevo = MAPPER.readValue(event.getBody(), User.class);

            String userId = UUID.randomUUID().toString();
            nuevo.setId(userId);

            Map<String, AttributeValue> itemValues = new HashMap<>();
            itemValues.put("id", AttributeValue.builder().s(userId).build());
            itemValues.put("name", AttributeValue.builder().s(nuevo.getName()).build());
            itemValues.put("email", AttributeValue.builder().s(nuevo.getEmail()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(itemValues)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

            String userJson = MAPPER.writeValueAsString(nuevo);
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .messageBody(userJson)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            UserResponse response = new UserResponse(
                    "Usuario creado con éxito en DynamoDB y encolado en SQS",
                    nuevo
            );

            return ResponseUtil.jsonResponse(201, response);

        } catch (Exception e) {
            context.getLogger().log("Error al crear usuario o enviar a SQS: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }
}