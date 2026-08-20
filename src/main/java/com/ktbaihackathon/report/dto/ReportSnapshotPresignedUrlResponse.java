package com.ktbaihackathon.report.dto;

public record ReportSnapshotPresignedUrlResponse(
        Long reportId,
        String objectKey,
        String uploadUrl,
        long expiresInSeconds
) {
}
