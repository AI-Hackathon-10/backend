package com.ktbaihackathon.voice.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class WhisperSttService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.whisper.api-url}")
    private String apiUrl;

    @Value("${openai.whisper.model}")
    private String model;

    @Value("${openai.whisper.language}")
    private String language;

    private RestClient whisperClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    private void init() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(30000);

        this.whisperClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public String transcribe(MultipartFile audioFile) {
        try {
            byte[] audioBytes = audioFile.getBytes();
            String rawFilename = audioFile.getOriginalFilename();
            String filename = (rawFilename == null || rawFilename.isBlank()) ? "audio.webm" : rawFilename;

            ByteArrayResource fileResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("model", model);
            body.add("language", language);

            String responseJson = whisperClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(responseJson);
            return node.get("text").asText();
        } catch (Exception e) {
            throw new CustomException(ResultCode.STT_SERVICE_UNAVAILABLE);
        }
    }
}
