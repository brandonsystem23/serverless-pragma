package com.pragma.infrastructure.out.sns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SnsNotificationAdapterTest {

    private SnsClient snsClient;
    private SnsNotificationAdapter adapter;

    @BeforeEach
    void setUp() {
        snsClient = mock(SnsClient.class);
        adapter = new SnsNotificationAdapter(snsClient, "topic-arn");
    }

    @Test
    void sendNotificationShouldPublishMessageToSns() {
        adapter.sendNotification("Asunto", "Mensaje de prueba");

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertEquals("topic-arn", request.topicArn());
        assertEquals("Asunto", request.subject());
        assertEquals("Mensaje de prueba", request.message());
    }
}
