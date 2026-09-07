package com.pragma.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.dto.UserResponse;
import com.pragma.model.User;
import com.pragma.util.ResponseUtil;

public class HandlerUpdate implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ObjectMapper MAPPER = new ObjectMapper();


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        try {
            String id = (event.getPathParameters() != null) ? event.getPathParameters().get("id") : null;

            if (id == null || id.isBlank()) {
                return ResponseUtil.errorResponse(400, "El id es obligatorio en la ruta");
            }

            if (event.getBody() == null || event.getBody().isBlank()) {
                return ResponseUtil.errorResponse(400, "El cuerpo de la solicitud es obligatorio");
            }

            User newDates = MAPPER.readValue(event.getBody(), User.class);


            UserResponse response = new UserResponse(
                    "Usuario actualizado con éxito en DynamoDB",
                    new User(
                            id,
                            newDates.getName(),
                            newDates.getEmail()
                    )
            );

            return ResponseUtil.jsonResponse(200, response);

        } catch (Exception e) {
            context.getLogger().log("Error al deserealizar: " + e.getMessage());
            return ResponseUtil.errorResponse(500, e.getMessage());
        }
    }

}