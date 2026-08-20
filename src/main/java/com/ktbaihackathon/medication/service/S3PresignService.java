package com.ktbaihackathon.medication.service;

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

        // 프론트엔드가 S3 업로드에 쓸 Presigned URL 발급
        String frontUrl = presignPut(frontKey);
        String backUrl = presignPut(backKey);

        // 🚨 [수정] 기획서 규격에 맞게 풀 URL 대신 순수 Object Key(frontKey, backKey) 자체를 영속화합니다.
        MedicationEntity medication = MedicationEntity.createRecognition(
                user,
                requestId,
                frontKey,
                backKey,
                null
        );

        MedicationEntity savedMedication = medicationRepository.save(medication);

        // 🚨 [수정] 바뀐 PK 게터 메서드명 적용
        return new PillUploadUrlsResponse(
                savedMedication.getMedicationRecognitionId(),
                requestId,
                frontUrl,
                backUrl
        );
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