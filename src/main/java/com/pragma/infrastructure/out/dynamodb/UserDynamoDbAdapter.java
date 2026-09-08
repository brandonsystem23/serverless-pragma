package com.pragma.infrastructure.out.dynamodb;

import com.pragma.domain.model.User;
import com.pragma.domain.spi.IUserPersistencePort;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDynamoDbAdapter implements IUserPersistencePort {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public UserDynamoDbAdapter(DynamoDbClient dynamoDbClient, String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public User save(User user) {
        Map<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("id", AttributeValue.builder().s(user.getId()).build());
        itemValues.put("name", AttributeValue.builder().s(user.getName()).build());
        itemValues.put("email", AttributeValue.builder().s(user.getEmail()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        dynamoDbClient.putItem(putItemRequest);
        return user;
    }

    @Override
    public List<User> findAll() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        return scanResponse.items().stream()
                .map(this::toUser)
                .toList();
    }

    @Override
    public User update(String id, User user) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

        boolean hasUpdates = false;

        if (user.getName() != null && !user.getName().isBlank()) {
            updateExpression.append("#n = :name");
            expressionAttributeNames.put("#n", "name");
            expressionAttributeValues.put(":name", AttributeValue.builder().s(user.getName()).build());
            hasUpdates = true;
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (hasUpdates) {
                updateExpression.append(", ");
            }
            updateExpression.append("email = :email");
            expressionAttributeValues.put(":email", AttributeValue.builder().s(user.getEmail()).build());
            hasUpdates = true;
        }

        UpdateItemRequest.Builder updateRequestBuilder = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression(updateExpression.toString())
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW);

        if (!expressionAttributeNames.isEmpty()) {
            updateRequestBuilder.expressionAttributeNames(expressionAttributeNames);
        }

        UpdateItemResponse response = dynamoDbClient.updateItem(updateRequestBuilder.build());

        return new User(
                getValue(response.attributes(), "id"),
                getValue(response.attributes(), "name"),
                getValue(response.attributes(), "email")
        );
    }

    @Override
    public void delete(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(deleteItemRequest);
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
