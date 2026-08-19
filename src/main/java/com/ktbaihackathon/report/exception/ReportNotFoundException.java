package com.ktbaihackathon.report.exception;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(Long reportId) {
        super("리포트를 찾을 수 없습니다: " + reportId);
    }
}
