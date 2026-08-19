package com.ktbaihackathon.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean isSuccess;
    private final String code;
    private final String message;
    private final T result;

    private ApiResponse(boolean isSuccess, String code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(String code, String message, T result) {
        return new ApiResponse<>(true, code, message, result);
    }

    // 성공 응답 (데이터 없음, result: null)
    public static <T> ApiResponse<T> success(String code, String message) {
        return new ApiResponse<>(true, code, message, null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public boolean isSuccess() { return isSuccess; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public T getResult() { return result; }
}