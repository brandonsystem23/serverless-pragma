package com.pragma.infrastructure.out.dynamodb;

import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDynamoDbAdapterTest {

    private DynamoDbClient dynamoDbClient;
    private UserDynamoDbAdapter adapter;

    @BeforeEach
    void setUp() {
        dynamoDbClient = mock(DynamoDbClient.class);
        adapter = new UserDynamoDbAdapter(dynamoDbClient, "users");
    }

    @Test
    void saveShouldPutItemAndReturnUser() {
        User user = new User("1", "Juan", "juan@mail.com");

        User result = adapter.save(user);

        assertEquals(user, result);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());

        PutItemRequest request = captor.getValue();
        assertEquals("users", request.tableName());
        assertEquals("1", request.item().get("id").s());
        assertEquals("Juan", request.item().get("name").s());
        assertEquals("juan@mail.com", request.item().get("email").s());
    }

    @Test
    void findAllShouldReturnMappedUsers() {
        Map<String, AttributeValue> item1 = Map.of(
                "id", AttributeValue.builder().s("1").build(),
                "name", AttributeValue.builder().s("Juan").build(),
                "email", AttributeValue.builder().s("juan@mail.com").build()
        );

        Map<String, AttributeValue> item2 = Map.of(
                "id", AttributeValue.builder().s("2").build(),
                "name", AttributeValue.builder().s("Ana").build(),
                "email", AttributeValue.builder().s("ana@mail.com").build()
        );

        ScanResponse scanResponse = ScanResponse.builder()
                .items(item1, item2)
                .build();

        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

        List<User> result = adapter.findAll();

        assertEquals(2, result.size());
        assertEquals(new User("1", "Juan", "juan@mail.com"), result.get(0));
        assertEquals(new User("2", "Ana", "ana@mail.com"), result.get(1));

        verify(dynamoDbClient).scan(any(ScanRequest.class));
    }

    @Test
    void updateShouldUpdateOnlyNameWhenOnlyNameIsProvided() {
        User user = new User(null, "Pedro", null);

        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(Map.of(
                        "id", AttributeValue.builder().s("123").build(),
                        "name", AttributeValue.builder().s("Pedro").build(),
                        "email", AttributeValue.builder().s("old@mail.com").build()
                ))
                .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        User result = adapter.update("123", user);

        assertEquals(new User("123", "Pedro", "old@mail.com"), result);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());

        UpdateItemRequest request = captor.getValue();
        assertEquals("users", request.tableName());
        assertEquals("123", request.key().get("id").s());
        assertTrue(request.updateExpression().contains("#n = :name"));
        assertEquals("Pedro", request.expressionAttributeValues().get(":name").s());
    }

    @Test
    void updateShouldUpdateOnlyEmailWhenOnlyEmailIsProvided() {
        User user = new User(null, null, "nuevo@mail.com");

        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(Map.of(
                        "id", AttributeValue.builder().s("123").build(),
                        "name", AttributeValue.builder().s("Juan").build(),
                        "email", AttributeValue.builder().s("nuevo@mail.com").build()
                ))
                .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        User result = adapter.update("123", user);

        assertEquals(new User("123", "Juan", "nuevo@mail.com"), result);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());

        UpdateItemRequest request = captor.getValue();
        assertEquals("users", request.tableName());
        assertEquals("123", request.key().get("id").s());
        assertTrue(request.updateExpression().contains("email = :email"));
        assertEquals("nuevo@mail.com", request.expressionAttributeValues().get(":email").s());
    }

    @Test
    void updateShouldUpdateNameAndEmailWhenBothAreProvided() {
        User user = new User(null, "Pedro", "pedro@mail.com");

        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(Map.of(
                        "id", AttributeValue.builder().s("123").build(),
                        "name", AttributeValue.builder().s("Pedro").build(),
                        "email", AttributeValue.builder().s("pedro@mail.com").build()
                ))
                .build();

        when(dynamoDbClient.updateItem(any(UpdateItemRequest.class))).thenReturn(response);

        User result = adapter.update("123", user);

        assertEquals(new User("123", "Pedro", "pedro@mail.com"), result);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(dynamoDbClient).updateItem(captor.capture());

        UpdateItemRequest request = captor.getValue();
        assertTrue(request.updateExpression().contains("#n = :name"));
        assertTrue(request.updateExpression().contains("email = :email"));
        assertEquals("Pedro", request.expressionAttributeValues().get(":name").s());
        assertEquals("pedro@mail.com", request.expressionAttributeValues().get(":email").s());
    }

    @Test
    void deleteShouldCallDeleteItem() {
        adapter.delete("123");

        ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
        verify(dynamoDbClient).deleteItem(captor.capture());

        DeleteItemRequest request = captor.getValue();
        assertEquals("users", request.tableName());
        assertEquals("123", request.key().get("id").s());
    }
}
