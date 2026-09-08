package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.application.dto.request.UserRequest;
import com.pragma.application.handler.IUserHandler;
import com.pragma.infrastructure.configuration.BeanConfiguration;
import com.pragma.infrastructure.exceptionhandler.LambdaExceptionHandler;
import com.pragma.infrastructure.util.ResponseUtil;

public class HandlerUpdate implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final IUserHandler userHandler;

    public HandlerUpdate() {
        this.userHandler = BeanConfiguration.userHandler();
    }

    HandlerUpdate(IUserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String id = event.getPathParameters() != null ? event.getPathParameters().get("id") : null;

            if (event.getBody() == null || event.getBody().isBlank()) {
                return ResponseUtil.errorResponse(400, "El cuerpo de la solicitud es obligatorio");
            }

            UserRequest request = MAPPER.readValue(event.getBody(), UserRequest.class);
            return ResponseUtil.jsonResponse(200, userHandler.updateUser(id, request));

        } catch (Exception e) {
            context.getLogger().log("Error al actualizar usuario: " + e.getMessage());
            return LambdaExceptionHandler.handle(e);
        }
    }
}
