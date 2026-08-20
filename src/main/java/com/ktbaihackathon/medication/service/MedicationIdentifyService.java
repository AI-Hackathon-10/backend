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
import com.ktbaihackathon.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationIdentifyService {

    private final S3ImageDownloadService s3ImageDownloadService;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    private RestClient fastApiClient;

    @PostConstruct
    private void initFastApiClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(60000);

        this.fastApiClient = RestClient.builder()
                .baseUrl(fastApiBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Transactional
    public List<MedicationIdentifyResponse> identify(Long userId, IdentifyRequest identifyRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.USER_NOT_FOUND));

        MedicationEntity medication = medicationRepository
                .findByRequestIdAndUserUserId(identifyRequest.requestId(), userId)
                .orElseThrow(() -> new CustomException(ResultCode.MEDICATION_NOT_FOUND));

        String frontKey = medication.getFrontImageObjectKey();
        String backKey = medication.getBackImageObjectKey();

        String frontBase64 = s3ImageDownloadService.downloadAsBase64(frontKey);
        String backBase64 = s3ImageDownloadService.downloadAsBase64(backKey);

        FastApiIdentifyRequest.User apiUser = new FastApiIdentifyRequest.User(
                user.getUserId(),
                user.getName(),
                user.getGender(),
                user.getBirthDate()
        );

        FastApiIdentifyRequest.Item item = new FastApiIdentifyRequest.Item(
                identifyRequest.requestId(),
                frontBase64,
                backBase64
        );

        List<String> symptomNames = identifyRequest.symptomTypes().stream()
                .map(Enum::name)
                .toList();

        FastApiIdentifyRequest apiRequest = new FastApiIdentifyRequest(
                apiUser,
                symptomNames,
                identifyRequest.startedAt(),
                List.of(item)
        );

        FastApiIdentifyResponse response;
        try {
            response = fastApiClient.post()
                    .uri("/identify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiRequest)
                    .retrieve()
                    .body(FastApiIdentifyResponse.class);

            if (response != null && response.result() != null && !response.result().isEmpty()) {
                String aiDrugName = response.result().get(0).itemName();
                medication.updateIdentificationResult(aiDrugName, "SUCCESS");
            } else {
                medication.updateIdentificationResult(null, "FAILED");
            }
        } catch (Exception e) {
            medication.updateIdentificationResult(null, "FAILED");
            throw e;
        }

        return response != null && response.result() != null
                ? response.result().stream().map(this::toMedicationIdentifyResponse).toList()
                : List.of();
    }

    private MedicationIdentifyResponse toMedicationIdentifyResponse(FastApiIdentifyResponse.Result result) {
        return new MedicationIdentifyResponse(
                result.ok(),
                result.itemSeq(),
                result.itemName(),
                result.imageUrl(),
                toIdentification(result.identification()),
                toRecommendation(result.recommendation()),
                toFeatures(result.features()),
                toOfficial(result.official()),
                result.document()
        );
    }

    private MedicationIdentifyResponse.Identification toIdentification(FastApiIdentifyResponse.Identification src) {
        if (src == null) return null;
        return new MedicationIdentifyResponse.Identification(src.confidence(), src.score());
    }

    private MedicationIdentifyResponse.Recommendation toRecommendation(FastApiIdentifyResponse.Recommendation src) {
        if (src == null) return null;
        return new MedicationIdentifyResponse.Recommendation(
                src.status(), src.score(), src.confidence(), src.reason(), src.caution()
        );
    }

    private MedicationIdentifyResponse.Features toFeatures(FastApiIdentifyResponse.Features src) {
        if (src == null) return null;
        return new MedicationIdentifyResponse.Features(
                src.frontImprint(), src.backImprint(), src.shape(), src.color(), src.scoreLine()
        );
    }

    private MedicationIdentifyResponse.Official toOfficial(FastApiIdentifyResponse.Official src) {
        if (src == null) return null;
        return new MedicationIdentifyResponse.Official(
                src.itemSeq(), src.itemName(), src.efficacy(), src.useMethod(),
                src.warning(), src.caution(), src.interaction(), src.sideEffect(),
                src.storage(), src.imageUrl()
        );
    }
}