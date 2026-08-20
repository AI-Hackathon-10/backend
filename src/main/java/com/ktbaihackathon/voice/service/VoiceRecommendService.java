package com.ktbaihackathon.voice.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import com.ktbaihackathon.voice.dto.FastApiSymptomRequest;
import com.ktbaihackathon.voice.dto.FastApiSymptomResponse;
import com.ktbaihackathon.voice.dto.VoiceRecommendRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class VoiceRecommendService {

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

    public FastApiSymptomResponse recommend(Long userId, VoiceRecommendRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.USER_NOT_FOUND));

        FastApiSymptomRequest apiRequest = new FastApiSymptomRequest(
                new FastApiSymptomRequest.User(
                        user.getUserId(),
                        user.getName(),
                        user.getGender(),
                        user.getBirthDate()
                ),
                new FastApiSymptomRequest.Voice(
                        request.symptomText(),
                        request.onsetText()
                )
        );

        try {
            return fastApiClient.post()
                    .uri("/symptoms/recommend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(apiRequest)
                    .retrieve()
                    .body(FastApiSymptomResponse.class);
        } catch (Exception e) {
            throw new CustomException(ResultCode.AI_SERVICE_UNAVAILABLE);
        }
    }
}
