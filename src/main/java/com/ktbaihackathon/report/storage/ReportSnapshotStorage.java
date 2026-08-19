package com.ktbaihackathon.report.storage;

import java.time.Duration;

public interface ReportSnapshotStorage {

    PresignedUpload createUploadUrl(String objectKey, Duration expiration);

    long verifyPng(String objectKey, long maximumSizeBytes);

    String createViewUrl(String objectKey, Duration expiration);
}
