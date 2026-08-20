package com.ktbaihackathon.report.dto;

public record ReportSnapshotUrlResponse(
        Long reportId,
        String snapshotUrl,
        long expiresInSeconds
) {
}
