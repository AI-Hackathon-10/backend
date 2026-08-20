package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.PillUploadUrlsResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional
    public PillUploadUrlsResponse generateUploadUrls(String sessionId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String requestId = UUID.randomUUID().toString();

        String frontKey = buildKey(sessionId, requestId, "front");
        String backKey = buildKey(sessionId, requestId, "back");

        String frontUrl = presignPut(frontKey);
        String backUrl = presignPut(backKey);

        // 🚨 [수정] .s3.amazonaws.com 에서 실제 사용 중인 서울 리전 주소(s3.ap-northeast-2.amazonaws.com)로 변경!
        String frontStorageUrl = "https://%s.s3.ap-northeast-2.amazonaws.com/%s".formatted(bucket, frontKey);
        String backStorageUrl = "https://%s.s3.ap-northeast-2.amazonaws.com/%s".formatted(bucket, backKey);

        MedicationEntity medication = MedicationEntity.createRecognition(
                user,
                requestId,
                frontStorageUrl,
                backStorageUrl,
                null
        );

        medicationRepository.save(medication);

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