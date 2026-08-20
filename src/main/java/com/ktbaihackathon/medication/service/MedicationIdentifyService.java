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
import org.springframework.http.MediaType;

@Service
@RequiredArgsConstructor
public class MedicationIdentifyService {

    private final S3ImageDownloadService s3ImageDownloadService;
    private final MedicationRepository medicationRepository;

    // 💡 [로컬 개발 환경 설정]: 내 PC 도커 포트로 연결된 FastAPI(localhost:8000) 및 타임아웃 60초 설정
    private final RestClient fastApiClient = RestClient.builder()
            .baseUrl("http://localhost:8000")
            .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                setConnectTimeout(5000);
                setReadTimeout(60000); // AI 연산 대기 시간 60초 보장
            }})
            .build();

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

        // 💡 수동 String.format을 지우고, 스네이크 케이스 필드명을 가진 DTO 레코드를 깔끔하게 생성
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
                    .body(apiRequest) // 👈 DTO 객체 바디 전달
                    .retrieve()
                    .body(FastApiIdentifyResponse.class);

            System.out.println("🎉 [디버그] 5. FastAPI 호출 성공! 200 OK 응답 받음");

            if (response != null) {
                String aiDrugName = null;

                // 1순위 추출: matchResult -> candidates 리스트의 첫 번째 아이템의 itemName
                if (response.matchResult() != null &&
                        response.matchResult().candidates() != null &&
                        !response.matchResult().candidates().isEmpty()) {

                    aiDrugName = response.matchResult().candidates().get(0).itemName();
                }
                // 2순위 추출 (Fallback): matchResult가 정상적이지 않을 때 detail 객체의 itemName 활용
                else if (response.detail() != null) {
                    aiDrugName = response.detail().itemName();
                }

                // 엔티티의 알약 이름 업데이트 및 상태값을 SUCCESS로 변경
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