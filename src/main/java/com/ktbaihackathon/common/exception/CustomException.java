package com.ktbaihackathon.common.exception;

import com.ktbaihackathon.common.response.ResultCode;

public class CustomException extends RuntimeException {

    private final ResultCode resultCode;

    public CustomException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}