package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.dto.UserResponse;
import com.pragma.model.User;
import com.pragma.util.ResponseUtil;
import java.util.UUID;

public class HandlerCreate implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            if (event.getBody() == null || event.getBody().isBlank()) {
                return ResponseUtil.errorResponse(400, "El cuerpo de la solicitud es obligatorio");
            }

            User nuevo = MAPPER.readValue(event.getBody(), User.class);

            String userId = UUID.randomUUID().toString();
            nuevo.setId(userId);

            UserResponse response = new UserResponse(
                    "Usuario creado con éxito en DynamoDB y encolado en SQS",
                    nuevo
            );

            return ResponseUtil.jsonResponse(201, response);

        } catch (Exception e) {
            context.getLogger().log("Error al deserealizar: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }
}