package com.pragma.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;

public final class ResponseUtil {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";

    private ResponseUtil() {

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static APIGatewayV2HTTPResponse jsonResponse(int statusCode, Object body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        try {
            response.setStatusCode(statusCode);
            response.setBody(MAPPER.writeValueAsString(body));
            response.setHeaders(Collections.singletonMap(CONTENT_TYPE, APPLICATION_JSON));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"Error serializando respuesta\"}");
            response.setHeaders(Collections.singletonMap(CONTENT_TYPE, APPLICATION_JSON));
        }
        return response;
    }

    public static APIGatewayV2HTTPResponse errorResponse(int statusCode, String message) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody("{\"error\":\"" + message + "\"}");
        response.setHeaders(Collections.singletonMap(CONTENT_TYPE, APPLICATION_JSON));
        return response;
    }
}
