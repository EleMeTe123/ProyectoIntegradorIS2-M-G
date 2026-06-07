package com.is1.proyecto.dto;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    private final Map<String, Object> data = new HashMap<>();

    public ApiResponse() {}

    public ApiResponse put(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse().put("message", message);
    }

    public static ApiResponse success(String message, String key, Object value) {
        return new ApiResponse().put("message", message).put(key, value);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse().put("error", message);
    }

    public Map<String, Object> toMap() {
        return data;
    }
}
