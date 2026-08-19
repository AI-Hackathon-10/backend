package com.ktbaihackathon.report.storage;

import com.ktbaihackathon.config.s3.S3Properties;
import com.ktbaihackathon.report.exception.InvalidReportRequestException;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3ReportSnapshotStorage implements ReportSnapshotStorage {

    private static final String PNG_CONTENT_TYPE = "image/png";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    @Override
    public PresignedUpload createUploadUrl(String objectKey, Duration expiration) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(objectKey)
            .contentType(PNG_CONTENT_TYPE)
            .build();
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .putObjectRequest(putObjectRequest)
                .build()
        );

        return new PresignedUpload(
            presignedRequest.url().toString(),
            Instant.now().plus(expiration)
        );
    }

    @Override
    public long verifyPng(String objectKey, long maximumSizeBytes) {
        try {
            HeadObjectResponse object = s3Client.headObject(
                HeadObjectRequest.builder().bucket(properties.getBucket()).key(objectKey).build()
            );
            long contentLength = object.contentLength();
            if (!PNG_CONTENT_TYPE.equalsIgnoreCase(object.contentType())) {
                throw new InvalidReportRequestException("문서 스냅샷은 PNG 파일이어야 합니다.");
            }
            if (contentLength <= 0 || contentLength > maximumSizeBytes) {
                throw new InvalidReportRequestException("문서 스냅샷 파일 크기가 허용 범위를 벗어났습니다.");
            }
            return contentLength;
        } catch (S3Exception exception) {
            throw new InvalidReportRequestException("S3에서 문서 스냅샷을 확인할 수 없습니다.");
        }
    }

    @Override
    public String createViewUrl(String objectKey, Duration expiration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(objectKey)
            .build();
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build()
        );
        return presignedRequest.url().toString();
    }
}
