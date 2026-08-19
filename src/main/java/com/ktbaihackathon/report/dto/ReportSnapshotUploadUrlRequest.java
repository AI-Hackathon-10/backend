package com.ktbaihackathon.report.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportSnapshotUploadUrlRequest(@NotBlank String contentType) {
}
