package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.FastApiIdentifyRequest;
import com.ktbaihackathon.medication.dto.FastApiIdentifyResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MedicationIdentifyService {

    private final S3ImageDownloadService s3ImageDownloadService;
    private final MedicationRepository medicationRepository;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    private RestClient fastApiClient;

    @PostConstruct
    private void initFastApiClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(60000); // AI 연산 대기 시간 60초 보장

        this.fastApiClient = RestClient.builder()
                .baseUrl(fastApiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public FastApiIdentifyResponse identify(Long userId, String requestId) {
        System.out.println("====== [디버그] 1. 서비스 진입 성공 ======");

        MedicationEntity medication = medicationRepository.findByRequestId(requestId)
                .orElseThrow(() -> new CustomException(ResultCode.MEDICATION_NOT_FOUND));

        System.out.println("====== [디버그] 2. DB에서 데이터 조회 성공 ======");

        String frontKey = medication.getFrontImageObjectKey();
        String backKey = medication.getBackImageObjectKey();

        System.out.println("====== [디버그] 3. S3에서 이미지 다운로드 시도 =====");
        String frontBase64 = s3ImageDownloadService.downloadAsBase64(frontKey);
        String backBase64 = s3ImageDownloadService.downloadAsBase64(backKey);

        System.out.println("====== [디버그] 4. S3 다운로드 완료, FastAPI 호출 시작 ====== ");

        FastApiIdentifyRequest apiRequest = new FastApiIdentifyRequest(
                frontBase64,
                backBase64,
                "image/jpeg",
                backBase64 != null ? "image/jpeg" : null
        );

        FastApiIdentifyResponse response;
        try {
            response = fastApiClient.post()
                    .uri("/identify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiRequest)
                    .retrieve()
                    .body(FastApiIdentifyResponse.class);

            System.out.println("🎉 [디버그] 5. FastAPI 호출 성공! 200 OK 응답 받음");

            if (response != null) {
                String aiDrugName = null;

                if (response.matchResult() != null &&
                        response.matchResult().candidates() != null &&
                        !response.matchResult().candidates().isEmpty()) {

                    aiDrugName = response.matchResult().candidates().get(0).itemName();
                } else if (response.detail() != null) {
                    aiDrugName = response.detail().itemName();
                }

                medication.updateIdentificationResult(aiDrugName, "SUCCESS");
            }
        } catch (Exception e) {
            System.out.println("❌ [디버그] FastAPI 호출 실패 -> 상태값 FAILED 변경. 에러 원인: " + e.getMessage());
            medication.updateIdentificationResult(null, "FAILED");
            throw e;
        }

        return response;
    }
}