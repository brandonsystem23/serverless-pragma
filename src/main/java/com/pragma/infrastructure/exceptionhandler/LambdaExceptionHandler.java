package com.pragma.infrastructure.exceptionhandler;

import com.pragma.domain.exception.ValidationException;
import com.pragma.infrastructure.util.ResponseUtil;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public class LambdaExceptionHandler {

    private LambdaExceptionHandler() {
    }

    public static APIGatewayV2HTTPResponse handle(Exception e) {
        if (e instanceof ValidationException) {
            return ResponseUtil.errorResponse(400, e.getMessage());
        }
        return ResponseUtil.errorResponse(500, e.getMessage());
    }
}
