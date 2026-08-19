package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.medication.dto.PillUploadUrlsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3PresignService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public PillUploadUrlsResponse generateUploadUrls(String sessionId) {
        String requestId = UUID.randomUUID().toString();

        String frontUrl = presignPut(buildKey(sessionId, requestId, "front"));
        String backUrl = presignPut(buildKey(sessionId, requestId, "back"));

        return new PillUploadUrlsResponse(requestId, frontUrl, backUrl);
    }

    private String buildKey(String sessionId, String requestId, String fileType) {
        return "%s/%s/%s.jpg".formatted(sessionId, requestId, fileType);
    }

    private String presignPut(String key) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/jpeg")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }
}