package com.ktbaihackathon.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportCreateRequest(
        @NotNull(message = "증상 기록 ID는 필수입니다.")
        Long symptomRecordId,

        @NotNull(message = "복용 약 ID는 필수입니다.")
        Long medicationId,

        @NotBlank(message = "리포트 내용은 필수입니다.")
        String summary
) {
}
