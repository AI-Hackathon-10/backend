package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.FastApiIdentifyRequest;
import com.ktbaihackathon.medication.dto.FastApiIdentifyResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MedicationIdentifyService {

    private final S3ImageDownloadService s3ImageDownloadService;
    private final MedicationRepository medicationRepository;

    private final RestClient fastApiClient = RestClient.builder()
            .baseUrl("http://localhost:8000")
            .build();

    @Transactional
    public FastApiIdentifyResponse identify(Long userId, String requestId) {
        System.out.println("====== [디버그] 1. 서비스 진입 성공 ======");

        MedicationEntity medication = medicationRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CustomException(ResultCode.MEDICATION_NOT_FOUND));

        System.out.println("====== [디버그] 2. DB에서 데이터 조회 성공 ======");

        String frontKey = extractKeyFromUrl(medication.getFrontImageUrl());
        String backKey = extractKeyFromUrl(medication.getBackImageUrl());

        System.out.println("====== [디버그] 3. S3에서 이미지 다운로드 시도 (Key: " + frontKey + ") ======");
        String frontBase64 = s3ImageDownloadService.downloadAsBase64(frontKey);
        String backBase64 = s3ImageDownloadService.downloadAsBase64(backKey);

        System.out.println("====== [디버그] 4. S3 다운로드 완료, FastAPI 호출 시작 ======");

        FastApiIdentifyRequest fastApiRequest = new FastApiIdentifyRequest(
                "data:image/jpeg;base64," + frontBase64,
                "data:image/jpeg;base64," + backBase64,
                "image/jpeg",
                "image/jpeg"
        );

        FastApiIdentifyResponse response = fastApiClient.post()
                .uri("/identify")
                .body(fastApiRequest)
                .retrieve()
                .body(FastApiIdentifyResponse.class);

        // TODO: response.matchResult()/detail() 실제 구조 확정되면 drugName 파싱해서 반영
        // medication.updateIdentificationResult(parsedDrugName);

        return response;
    }

    private String extractKeyFromUrl(String storageUrl) {
        // ".amazonaws.com/" 문구의 시작 위치를 찾습니다.
        int index = storageUrl.indexOf(".amazonaws.com/");
        if (index == -1) {
            throw new IllegalArgumentException("올바르지 않은 S3 URL 형식입니다: " + storageUrl);
        }

        // ".amazonaws.com/" 글자 수(15글자)만큼 더한 인덱스부터 끝까지 잘라냅니다.
        String extractedKey = storageUrl.substring(index + ".amazonaws.com/".length());

        System.out.println("🔑 [디버그] 추출된 진짜 S3 Key: " + extractedKey);
        return extractedKey;
    }
}