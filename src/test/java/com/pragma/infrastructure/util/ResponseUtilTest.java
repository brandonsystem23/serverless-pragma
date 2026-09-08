package com.pragma.infrastructure.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilTest {

    @AfterEach
    void tearDown() {
        ResponseUtil.resetMapper();
    }

    @Test
    void jsonResponseShouldBuildCorrectResponse() {
        APIGatewayV2HTTPResponse response = ResponseUtil.jsonResponse(200, Map.of("message", "ok"));

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("ok"));
    }

    @Test
    void jsonResponseShouldReturn500WhenSerializationFails() {
        ObjectMapper failingMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) {
                throw new RuntimeException("serialization error");
            }
        };

        ResponseUtil.setMapper(failingMapper);

        APIGatewayV2HTTPResponse response = ResponseUtil.jsonResponse(200, Map.of("message", "ok"));

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\":\"Error serializando respuesta\"}", response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void errorResponseShouldBuildCorrectResponse() {
        APIGatewayV2HTTPResponse response = ResponseUtil.errorResponse(400, "Bad request");

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\":\"Bad request\"}", response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }
}
