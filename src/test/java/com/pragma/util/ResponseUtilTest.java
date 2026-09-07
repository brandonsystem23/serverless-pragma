package com.pragma.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseUtilTest {

    @AfterEach
    void tearDown() {
        ResponseUtil.resetMapper();
    }

    @Test
    void shouldBuildJsonResponseCorrectly() {
        APIGatewayV2HTTPResponse response = ResponseUtil.jsonResponse(200, Map.of("message", "ok"));

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("ok"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldReturn500WhenSerializationFails() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsString(any())).thenThrow(new RuntimeException("serialization error"));

        ResponseUtil.setMapper(mockMapper);

        APIGatewayV2HTTPResponse response = ResponseUtil.jsonResponse(200, new Object());

        assertEquals(500, response.getStatusCode());
        assertEquals("{\"error\":\"Error serializando respuesta\"}", response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }

    @Test
    void shouldBuildErrorResponseCorrectly() {
        APIGatewayV2HTTPResponse response = ResponseUtil.errorResponse(400, "bad request");

        assertEquals(400, response.getStatusCode());
        assertEquals("{\"error\":\"bad request\"}", response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
    }
}
