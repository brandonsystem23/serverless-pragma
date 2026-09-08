package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.dto.response.MessageResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.infrastructure.configuration.BeanConfiguration;
import com.pragma.infrastructure.exceptionhandler.LambdaExceptionHandler;
import com.pragma.infrastructure.util.ResponseUtil;

public class HandlerDelete implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final IUserHandler userHandler;

    public HandlerDelete() {
        this.userHandler = BeanConfiguration.userHandler();
    }

    HandlerDelete(IUserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String id = event.getPathParameters() != null ? event.getPathParameters().get("id") : null;
            userHandler.deleteUser(id);
            return ResponseUtil.jsonResponse(200,
                    new MessageResponse("Usuario con id " + id + " eliminado correctamente de DynamoDB"));
        } catch (Exception e) {
            context.getLogger().log("Error al eliminar usuario: " + e.getMessage());
            return LambdaExceptionHandler.handle(e);
        }
    }
}
