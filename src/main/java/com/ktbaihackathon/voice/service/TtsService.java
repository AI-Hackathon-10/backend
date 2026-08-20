package com.ktbaihackathon.voice.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TtsService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.tts.api-url}")
    private String apiUrl;

    @Value("${openai.tts.model}")
    private String model;

    @Value("${openai.tts.voice}")
    private String voice;

    private RestClient ttsClient;

    @PostConstruct
    private void init() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(30000);

        this.ttsClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public byte[] synthesize(String text) {
        try {
            Map<String, String> requestBody = Map.of(
                    "model", model,
                    "input", text,
                    "voice", voice
            );

            return ttsClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            throw new CustomException(ResultCode.TTS_SERVICE_UNAVAILABLE);
        }
    }
}
