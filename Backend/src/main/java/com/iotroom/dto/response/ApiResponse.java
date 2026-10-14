package com.iotroom.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(Integer code, String message, T result) {
    public static <T> ApiResponse<T> ok(T result) {
        return new ApiResponse<>(200, "success", result);
    }

    public static <T> ApiResponse<T> ok(String message, T result) {
        return new ApiResponse<>(200, message, result);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

