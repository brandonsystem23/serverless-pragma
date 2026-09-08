package com.pragma.infrastructure.out.sqs;

import com.pragma.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqsQueueAdapterTest {

    private SqsClient sqsClient;
    private SqsQueueAdapter adapter;

    @BeforeEach
    void setUp() {
        sqsClient = mock(SqsClient.class);
        adapter = new SqsQueueAdapter(sqsClient, "queue-url");
    }

    @Test
    void sendUserCreatedShouldSerializeAndSendMessage() {
        User user = new User("1", "Juan", "juan@mail.com");

        adapter.sendUserCreated(user);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertEquals("queue-url", request.queueUrl());
        assertTrue(request.messageBody().contains("\"id\":\"1\""));
        assertTrue(request.messageBody().contains("\"name\":\"Juan\""));
        assertTrue(request.messageBody().contains("\"email\":\"juan@mail.com\""));
    }

    @Test
    void sendUserCreatedShouldThrowRuntimeExceptionWhenSendFails() {
        User user = new User("1", "Juan", "juan@mail.com");

        doThrow(new RuntimeException("AWS error"))
                .when(sqsClient).sendMessage(any(SendMessageRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> adapter.sendUserCreated(user));

        assertEquals("Error publicando usuario en SQS", exception.getMessage());
        assertNotNull(exception.getCause());
    }
}
