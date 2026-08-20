package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException; // 프로젝트 에러 핸들링 패키지에 맞게 수정
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.PillUploadUrlsResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository; // 👈 유저 레포지토리 패키지 경로 맞추기
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
    private final MedicationRepository medicationRepository; //  레포지토리 주입
    private final UserRepository userRepository;             //  유저 조회를 위해 주입

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Transactional // 🌟 DB 저장을 위해 트랜잭션 추가
    public PillUploadUrlsResponse generateUploadUrls(String sessionId, Long userId) {
        // 1. 토큰에서 추출한 userId로 유저 엔티티 조회 (유저 없으면 에러 처리)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String requestId = UUID.randomUUID().toString();

        String frontKey = buildKey(sessionId, requestId, "front");
        String backKey = buildKey(sessionId, requestId, "back");

        String frontUrl = presignPut(frontKey);
        String backUrl = presignPut(backKey);

        // 2. S3 실제 객체 주소 조립
        String frontStorageUrl = "https://%s.s3.amazonaws.com/%s".formatted(bucket, frontKey);
        String backStorageUrl = "https://%s.s3.amazonaws.com/%s".formatted(bucket, backKey);

        // 3. 정적 팩토리 메서드 `createRecognition` 사용해서 엔티티 생성!
        // (아직 알약 이름은 모르니까 일단 null이나 빈 값으로 세팅)
        MedicationEntity medication = MedicationEntity.createRecognition(
                user,
                requestId,
                frontStorageUrl,
                backStorageUrl,
                null
        );

        // 4. 데이터베이스에 영속화 (Insert 쿼리 실행)
        MedicationEntity savedMedication = medicationRepository.save(medication);

        return new PillUploadUrlsResponse(
                savedMedication.getDrugRecognitionId(),
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
