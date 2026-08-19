package com.ktbaihackathon.common.response;

public enum ResultCode {

    MEDICATION_ANALYSIS_SUCCESS("의약품 분석에 성공했습니다."),
    MEDICATION_NOT_FOUND("의약품 정보를 찾을 수 없습니다."),
    IDENTIFICATION_LOW_CONFIDENCE("식별 신뢰도가 낮습니다. 재촬영이 필요합니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    ResultCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}