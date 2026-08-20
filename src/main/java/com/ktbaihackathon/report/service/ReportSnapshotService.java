package com.ktbaihackathon.report.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.report.dto.ReportSnapshotPresignedUrlResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUrlResponse;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
public class ReportSnapshotService {

    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(5);
    private static final long PRESIGN_EXPIRES_IN_SECONDS = 300L;

    private final S3Presigner s3Presigner;
    private final ReportRepository reportRepository;
    private final String bucket;

    public ReportSnapshotService(
            S3Presigner s3Presigner,
            ReportRepository reportRepository,
            @Value("${aws.s3.bucket}") String bucket
    ) {
        this.s3Presigner = s3Presigner;
        this.reportRepository = reportRepository;
        this.bucket = bucket;
    }

    @Transactional
    public ReportSnapshotPresignedUrlResponse issueUploadUrl(
            Long userId,
            Long reportId
    ) {
        Report report = findOwnedReport(userId, reportId);
        String objectKey = buildObjectKey(userId, reportId);
        String uploadUrl = presignPut(objectKey);

        report.prepareSnapshotUpload(objectKey);
        reportRepository.save(report);

        return new ReportSnapshotPresignedUrlResponse(
                reportId,
                objectKey,
                uploadUrl,
                PRESIGN_EXPIRES_IN_SECONDS
        );
    }

    @Transactional
    public void completeUpload(Long userId, Long reportId) {
        Report report = findOwnedReport(userId, reportId);

        if (isBlank(report.getSnapshotObjectKey())) {
            throw new CustomException(ResultCode.REPORT_SNAPSHOT_NOT_AVAILABLE);
        }

        report.completeSnapshotUpload();
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public ReportSnapshotUrlResponse getSnapshotUrl(Long userId, Long reportId) {
        Report report = findOwnedReport(userId, reportId);

        if (report.getSnapshotStatus() != ReportSnapshotStatus.UPLOADED
                || isBlank(report.getSnapshotObjectKey())) {
            throw new CustomException(ResultCode.REPORT_SNAPSHOT_NOT_AVAILABLE);
        }

        String snapshotUrl = presignGet(report.getSnapshotObjectKey());
        return new ReportSnapshotUrlResponse(
                reportId,
                snapshotUrl,
                PRESIGN_EXPIRES_IN_SECONDS
        );
    }

    private Report findOwnedReport(Long userId, Long reportId) {
        return reportRepository.findByReportIdAndUser_UserId(reportId, userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_REQUEST));
    }

    private String buildObjectKey(Long userId, Long reportId) {
        return "reports/%d/%d/snapshot.png".formatted(userId, reportId);
    }

    private String presignPut(String objectKey) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType("image/png")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .putObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest)
                .url()
                .toString();
    }

    private String presignGet(String objectKey) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(objectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
