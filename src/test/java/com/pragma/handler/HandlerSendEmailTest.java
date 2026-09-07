package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HandlerSendEmailTest {

    @Test
    void shouldCreateHandlerWithDefaultConstructor() {
        HandlerSendEmail handler = new HandlerSendEmail();
        assertNotNull(handler);
    }

    @Test
    void shouldPublishMessageWhenSqsMessageIsValid() {
        SnsClient snsClient = mock(SnsClient.class);
        HandlerSendEmail handler = new HandlerSendEmail(snsClient);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody("""
                {
                  "id": "1",
                  "name": "Juan Perez",
                  "email": "juan@example.com"
                }
                """);

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(sqsMessage));

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        Void response = handler.handleRequest(event, context);

        assertNull(response);
        verify(snsClient).publish(any(PublishRequest.class));
        verify(logger, atLeastOnce()).log(anyString());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenJsonIsInvalid() {
        SnsClient snsClient = mock(SnsClient.class);
        HandlerSendEmail handler = new HandlerSendEmail(snsClient);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody("{invalid json}");

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(sqsMessage));

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> handler.handleRequest(event, context));

        assertNotNull(exception);
        verify(logger).log(contains("Error al procesar el mensaje SQS o publicar en SNS"));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSnsFails() {
        SnsClient snsClient = mock(SnsClient.class);
        doThrow(new RuntimeException("SNS error"))
                .when(snsClient)
                .publish(any(PublishRequest.class));

        HandlerSendEmail handler = new HandlerSendEmail(snsClient);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody("""
                {
                  "id": "1",
                  "name": "Juan Perez",
                  "email": "juan@example.com"
                }
                """);

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(sqsMessage));

        LambdaLogger logger = mock(LambdaLogger.class);
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(logger);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> handler.handleRequest(event, context));

        assertNotNull(exception);
        verify(logger).log(contains("Error al procesar el mensaje SQS o publicar en SNS"));
    }
}
