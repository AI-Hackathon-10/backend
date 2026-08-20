package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.FastApiIdentifyRequest;
import com.ktbaihackathon.medication.dto.FastApiIdentifyResponse;
import com.ktbaihackathon.medication.dto.IdentifyRequest;
import com.ktbaihackathon.medication.dto.MedicationIdentifyResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Objects;

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
    public List<MedicationIdentifyResponse> identify(Long userId, IdentifyRequest request) {
        System.out.println("====== [디버그] 1. 서비스 진입 성공 ======");

        MedicationEntity medication = medicationRepository.findByRequestIdAndUserUserId(request.requestId(), userId)
                .orElseThrow(() -> new CustomException(ResultCode.MEDICATION_NOT_FOUND));

        System.out.println("====== [디버그] 2. DB에서 데이터 조회 성공 ======");

        String frontKey = medication.getFrontImageObjectKey();
        String backKey = medication.getBackImageObjectKey();
        System.out.println("====== [디버그] 3. S3에서 이미지 다운로드 시도 =====");
        String frontBase64 = s3ImageDownloadService.downloadAsBase64(frontKey);
        String backBase64 = s3ImageDownloadService.downloadAsBase64(backKey);

        System.out.println("====== [디버그] 4. S3 다운로드 완료, FastAPI 호출 시작 ====== ");

        FastApiIdentifyRequest apiRequest = createFastApiRequest(
                medication, request, frontBase64, backBase64
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

                if (response.result() != null && !response.result().isEmpty()) {
                    aiDrugName = response.result().get(0).itemName();
                }

                // 엔티티의 알약 이름 업데이트 및 상태값을 SUCCESS로 변경
                medication.updateIdentificationResult(aiDrugName, "SUCCESS");
            }
        } catch (Exception e) {
            System.out.println("❌ [디버그] FastAPI 호출 실패 -> 상태값 FAILED 변경. 에러 원인: " + e.getMessage());
            medication.updateIdentificationResult(null, "FAILED");
            throw e;
        }

        return toServiceResponse(response);
    }

    FastApiIdentifyRequest createFastApiRequest(
            MedicationEntity medication,
            IdentifyRequest request,
            String frontBase64,
            String backBase64
    ) {
        User user = medication.getUser();
        FastApiIdentifyRequest.User apiUser = new FastApiIdentifyRequest.User(
                user.getUserId(),
                user.getName(),
                user.getGender(),
                user.getBirthDate()
        );
        List<String> symptoms = request.symptomTypes().stream()
                .map(Enum::name)
                .toList();
        FastApiIdentifyRequest.Item item = new FastApiIdentifyRequest.Item(
                String.valueOf(medication.getMedicationRecognitionId()),
                toImageDataUrl(frontBase64),
                toImageDataUrl(backBase64)
        );

        return new FastApiIdentifyRequest(
                apiUser,
                symptoms,
                request.startedAt(),
                List.of(item)
        );
    }

    private String toImageDataUrl(String base64) {
        return base64 == null ? null : "data:image/jpeg;base64," + base64;
    }

    private List<MedicationIdentifyResponse> toServiceResponse(FastApiIdentifyResponse response) {
        if (response == null || response.result() == null) {
            return List.of();
        }
        return response.result().stream()
                .filter(Objects::nonNull)
                .map(this::toServiceResponse)
                .toList();
    }

    private MedicationIdentifyResponse toServiceResponse(FastApiIdentifyResponse.Result result) {
        FastApiIdentifyResponse.Identification identification = result.identification();
        FastApiIdentifyResponse.Recommendation recommendation = result.recommendation();
        FastApiIdentifyResponse.Features features = result.features();
        FastApiIdentifyResponse.Official official = result.official();

        return new MedicationIdentifyResponse(
                result.ok(), result.itemSeq(), result.itemName(), result.imageUrl(),
                identification == null ? null : new MedicationIdentifyResponse.Identification(
                        identification.confidence(), identification.score()),
                recommendation == null ? null : new MedicationIdentifyResponse.Recommendation(
                        recommendation.status(), recommendation.score(), recommendation.confidence(),
                        recommendation.reason(), recommendation.caution()),
                features == null ? null : new MedicationIdentifyResponse.Features(
                        features.frontImprint(), features.backImprint(), features.shape(),
                        features.color(), features.scoreLine()),
                official == null ? null : new MedicationIdentifyResponse.Official(
                        official.itemSeq(), official.itemName(), official.efficacy(), official.useMethod(),
                        official.warning(), official.caution(), official.interaction(), official.sideEffect(),
                        official.storage(), official.imageUrl()),
                result.document()
        );
    }
}
