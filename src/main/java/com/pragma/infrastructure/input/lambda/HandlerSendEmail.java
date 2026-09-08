package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.application.handler.IUserHandler;
import com.pragma.domain.model.User;
import com.pragma.infrastructure.configuration.BeanConfiguration;

public class HandlerSendEmail implements RequestHandler<SQSEvent, Void> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final IUserHandler userHandler;

    public HandlerSendEmail() {
        this.userHandler = BeanConfiguration.userHandler();
    }

    HandlerSendEmail(IUserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                String body = message.getBody();
                context.getLogger().log("Mensaje recibido desde SQS: " + body);

                User user = MAPPER.readValue(body, User.class);
                userHandler.notifyUserCreated(user);

                context.getLogger().log("Mensaje publicado exitosamente en SNS para distribución por correo.");
            } catch (Exception e) {
                context.getLogger().log("Error al procesar el mensaje SQS o publicar en SNS: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
