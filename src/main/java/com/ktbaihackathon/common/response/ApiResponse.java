package com.ktbaihackathon.common.response;

public record ApiResponse<T>(T result) {

    public static <T> ApiResponse<T> of(T result) {
        return new ApiResponse<>(result);
    }
}
