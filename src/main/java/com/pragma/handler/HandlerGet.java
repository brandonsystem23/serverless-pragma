package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pragma.model.User;
import com.pragma.util.ResponseUtil;

import java.util.List;

public class HandlerGet implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
            List<User> users = List.of(
                    new User("f7ef33fe-2fbc-47e5-9ef3-0fccb3214e18", "Brandon Briones", "brandonbr1208@gmail.com"),
                    new User("5962eda9-f602-4788-a65c-2f00456168a1", "Carlos Ruiz", "carlos1234@gmail.com"),
                    new User("f222e6f2-735d-4ec8-945b-27b354d007a5", "Piero Arenas", "piero1209_arenas@gmail.com")
            );

            return ResponseUtil.jsonResponse(200, users);

    }
}