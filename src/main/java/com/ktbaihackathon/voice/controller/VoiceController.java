package com.ktbaihackathon.voice.controller;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.common.security.SecurityContextUtil;
import com.ktbaihackathon.voice.dto.FastApiSymptomResponse;
import com.ktbaihackathon.voice.dto.TranscribeResponse;
import com.ktbaihackathon.voice.dto.TtsRequest;
import com.ktbaihackathon.voice.dto.VoiceRecommendRequest;
import com.ktbaihackathon.voice.service.TtsService;
import com.ktbaihackathon.voice.service.VoiceRecommendService;
import com.ktbaihackathon.voice.service.WhisperSttService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
public class VoiceController {

    private static final long MAX_AUDIO_SIZE = 25 * 1024 * 1024; // 25MB

    private final WhisperSttService whisperSttService;
    private final VoiceRecommendService voiceRecommendService;
    private final TtsService ttsService;

    @PostMapping("/transcribe")
    public ResponseEntity<ApiResponse<TranscribeResponse>> transcribe(
            HttpServletRequest request,
            @RequestParam("audio") MultipartFile audio) {

        SecurityContextUtil.extractUserId(request);

        if (audio.getSize() > MAX_AUDIO_SIZE) {
            throw new CustomException(ResultCode.AUDIO_FILE_TOO_LARGE);
        }

        String text = whisperSttService.transcribe(audio);
        TranscribeResponse result = new TranscribeResponse(text);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ResultCode.VOICE_TRANSCRIBE_SUCCESS.name(),
                        ResultCode.VOICE_TRANSCRIBE_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @PostMapping("/recommend")
    public ResponseEntity<ApiResponse<FastApiSymptomResponse>> recommend(
            HttpServletRequest request,
            @RequestBody VoiceRecommendRequest voiceRequest) {

        Long userId = SecurityContextUtil.extractUserId(request);
        FastApiSymptomResponse result = voiceRecommendService.recommend(userId, voiceRequest);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ResultCode.VOICE_RECOMMEND_SUCCESS.name(),
                        ResultCode.VOICE_RECOMMEND_SUCCESS.getMessage(),
                        result
                )
        );
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> tts(
            HttpServletRequest request,
            @RequestBody TtsRequest ttsRequest) {

        SecurityContextUtil.extractUserId(request);

        byte[] audioBytes = ttsService.synthesize(ttsRequest.text());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }
}
