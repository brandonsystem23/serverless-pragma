package com.pragma.infrastructure.input.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.application.handler.IUserHandler;
import com.pragma.infrastructure.configuration.BeanConfiguration;
import com.pragma.infrastructure.exceptionhandler.LambdaExceptionHandler;
import com.pragma.infrastructure.util.ResponseUtil;

public class HandlerGet implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final IUserHandler userHandler;

    public HandlerGet() {
        this.userHandler = BeanConfiguration.userHandler();
    }

    HandlerGet(IUserHandler userHandler) {
        this.userHandler = userHandler;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            return ResponseUtil.jsonResponse(200, userHandler.getAllUsers());
        } catch (Exception e) {
            context.getLogger().log("Error al consultar usuarios: " + e.getMessage());
            return LambdaExceptionHandler.handle(e);
        }
    }
}
