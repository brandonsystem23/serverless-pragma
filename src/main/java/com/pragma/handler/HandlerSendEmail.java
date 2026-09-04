package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.model.User;
import com.pragma.util.SnsClientProvider;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class HandlerSendEmail implements RequestHandler<SQSEvent, Void> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SnsClient snsClient = SnsClientProvider.getClient();

    private static final String TOPIC_ARN = System.getenv("TOPIC_ARN") != null ?
            System.getenv("TOPIC_ARN") : "arn:aws:sns:us-east-1:121604171970:user-events-topic";

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            try {
                String body = message.getBody();
                context.getLogger().log("Mensaje recibido desde SQS: " + body);

                User user = MAPPER.readValue(body, User.class);

                String formattedMessage = String.format(
                        "¡Hola!\n\nSe ha creado un usuario nuevo exitosamente.\n\n" +
                                "• Nombre: %s\n" +
                                "• Correo: %s\n" +
                                "• ID: %s\n\n" +
                                "¡Bienvenido a la plataforma!",
                        user.getName(), user.getEmail(), user.getId()
                );

                context.getLogger().log("Procesando usuario para notificar vía SNS: " + user.getEmail()
                        + " | Nombre: " + user.getName());

                PublishRequest publishRequest = PublishRequest.builder()
                        .topicArn(TOPIC_ARN)
                        .subject("¡Notificación de nuevo usuario!")
                        .message(formattedMessage)
                        .build();

                snsClient.publish(publishRequest);

                context.getLogger().log("Mensaje publicado exitosamente en SNS para distribución por correo.");

            } catch (Exception e) {
                context.getLogger().log("Error al procesar el mensaje SQS o publicar en SNS: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}