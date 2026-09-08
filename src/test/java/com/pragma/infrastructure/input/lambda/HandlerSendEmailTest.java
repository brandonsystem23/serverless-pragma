package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.pragma.application.handler.IUserHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlerSendEmailTest {

    private IUserHandler userHandler;
    private HandlerSendEmail handler;
    private Context context;
    private LambdaLogger logger;

    @BeforeEach
    void setUp() {
        userHandler = mock(IUserHandler.class);
        handler = new HandlerSendEmail(userHandler);
        context = mock(Context.class);
        logger = mock(LambdaLogger.class);
        when(context.getLogger()).thenReturn(logger);
    }

    @Test
    void handleRequestShouldProcessAllMessagesSuccessfully() {
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("{\"id\":\"1\",\"name\":\"Juan\",\"email\":\"juan@mail.com\"}");

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(message));

        Void result = handler.handleRequest(event, context);

        assertNull(result);
        verify(userHandler).notifyUserCreated(argThat(user ->
                "1".equals(user.getId()) &&
                        "Juan".equals(user.getName()) &&
                        "juan@mail.com".equals(user.getEmail())
        ));
        verify(logger).log(contains("Mensaje recibido desde SQS"));
        verify(logger).log(contains("Mensaje publicado exitosamente en SNS"));
    }

    @Test
    void handleRequestShouldThrowRuntimeExceptionWhenMessageProcessingFails() {
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setBody("{bad-json}");

        SQSEvent event = new SQSEvent();
        event.setRecords(List.of(message));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> handler.handleRequest(event, context));

        assertNotNull(exception);
        verify(logger).log(contains("Error al procesar el mensaje SQS o publicar en SNS"));
    }
}
