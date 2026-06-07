package com.is1.proyecto.dto;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successCreatesResponseWithMessage() {
        Map<String, Object> result = ApiResponse.success("ok").toMap();
        assertEquals("ok", result.get("message"));
        assertNull(result.get("error"));
    }

    @Test
    void successWithExtraKey() {
        Map<String, Object> result = ApiResponse.success("created", "id", 42).toMap();
        assertEquals("created", result.get("message"));
        assertEquals(42, result.get("id"));
    }

    @Test
    void errorCreatesResponseWithError() {
        Map<String, Object> result = ApiResponse.error("something went wrong").toMap();
        assertEquals("something went wrong", result.get("error"));
        assertNull(result.get("message"));
    }

    @Test
    void putReturnsThisForChaining() {
        ApiResponse res = new ApiResponse();
        assertSame(res, res.put("a", 1));
    }

    @Test
    void putStoresValue() {
        Map<String, Object> result = new ApiResponse().put("key", "value").toMap();
        assertEquals("value", result.get("key"));
    }

    @Test
    void putOverwritesExistingKey() {
        Map<String, Object> result = new ApiResponse()
                .put("x", 1)
                .put("x", 2)
                .toMap();
        assertEquals(2, result.get("x"));
    }

    @Test
    void emptyResponseHasNoKeys() {
        Map<String, Object> result = new ApiResponse().toMap();
        assertTrue(result.isEmpty());
    }
}
