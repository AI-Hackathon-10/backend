package com.ktbaihackathon.common.response;

public enum ResultCode {

    MEDICATION_ANALYSIS_SUCCESS("의약품 분석에 성공했습니다."),
    MEDICATION_INTAKE_SUCCESS("복용 기록이 저장되었습니다."),
    MEDICATION_NOT_FOUND("의약품 정보를 찾을 수 없습니다."),
    IDENTIFICATION_LOW_CONFIDENCE("식별 신뢰도가 낮습니다. 재촬영이 필요합니다."),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."),
    USER_CREATE_SUCCESS("회원 가입에 성공했습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    DUPLICATE_LOGIN_ID("이미 사용 중인 아이디입니다."),
    PRESIGNED_URL_ISSUED("업로드용 URL이 발급되었습니다."),
    UNAUTHORIZED("인증 정보가 없거나 올바르지 않습니다."),
    INVALID_TOKEN("유효하지 않거나 만료된 토큰입니다."),
    LOGIN_SUCCESS("로그인에 성공했습니다."),
    LOGIN_FAILED("아이디 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_REISSUE_SUCCESS("토큰이 재발급되었습니다."),
    LOGOUT_SUCCESS("로그아웃되었습니다."),
    AUTH_CHECK_SUCCESS("인증 확인에 성공했습니다."),
    SYMPTOM_RECORD_CREATE_SUCCESS("증상 기록이 저장되었습니다."),
    REPORT_CREATE_SUCCESS("리포트가 저장되었습니다."),
    REPORT_QUERY_SUCCESS("리포트를 조회했습니다."),
    REPORT_MEDICATION_NOT_TAKEN("복용 기록을 먼저 확정해야 리포트를 생성할 수 있습니다."),
    REPORT_SNAPSHOT_PRESIGNED_URL_SUCCESS("리포트 스냅샷 업로드 URL이 발급되었습니다."),
    REPORT_SNAPSHOT_COMPLETE_SUCCESS("리포트 스냅샷 업로드가 완료되었습니다."),
    REPORT_SNAPSHOT_URL_SUCCESS("리포트 스냅샷 조회 URL이 발급되었습니다."),
    REPORT_SNAPSHOT_NOT_AVAILABLE("리포트 스냅샷을 아직 사용할 수 없습니다."),
    USER_NOT_FOUND("존재하지 않는 유저입니다."),

    VOICE_TRANSCRIBE_SUCCESS("음성 인식에 성공했습니다."),
    VOICE_RECOMMEND_SUCCESS("음성 증상 기반 추천에 성공했습니다."),
    STT_SERVICE_UNAVAILABLE("음성 인식 서비스에 일시적으로 연결할 수 없습니다."),
    AI_SERVICE_UNAVAILABLE("AI 추천 서비스에 일시적으로 연결할 수 없습니다."),
    AUDIO_FILE_TOO_LARGE("오디오 파일이 너무 큽니다. 60초 이내로 녹음해 주세요."),

    TTS_SUCCESS("음성 합성에 성공했습니다."),
    TTS_SERVICE_UNAVAILABLE("음성 합성 서비스에 일시적으로 연결할 수 없습니다.");

    private final String message;

    ResultCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
