package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.storage.PresignedUpload;

public record ReportSnapshotUploadUrlResponse(
    String uploadUrl,
    String objectKey,
    String fileName,
    String contentType,
    java.time.Instant expiresAt
) {

    public static ReportSnapshotUploadUrlResponse from(
        PresignedUpload upload,
        String objectKey,
        String fileName
    ) {
        return new ReportSnapshotUploadUrlResponse(
            upload.uploadUrl(),
            objectKey,
            fileName,
            "image/png",
            upload.expiresAt()
        );
    }
}
