package com.ktbaihackathon.medication.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.common.security.SecurityContextUtil; //
import com.ktbaihackathon.medication.dto.IdentifyRequest;
import com.ktbaihackathon.medication.dto.MedicationIdentifyResponse;
import com.ktbaihackathon.medication.dto.PillUploadUrlsResponse;
import com.ktbaihackathon.medication.dto.PresignUrlRequest;
import com.ktbaihackathon.medication.service.MedicationIdentifyService;
import com.ktbaihackathon.medication.service.S3PresignService;
import jakarta.servlet.http.HttpServletRequest; //
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationUploadController {

    private final S3PresignService s3PresignService;
    private final MedicationIdentifyService medicationIdentifyService;


    @PostMapping("/upload/presigned-url")
    public ResponseEntity<ApiResponse<PillUploadUrlsResponse>> getPresignedUrls(
            HttpServletRequest request, // 👈 토큰 정보를 파싱하기 위해 톰캣 request 객체를 직접 받음
            @RequestBody PresignUrlRequest presignUrlRequest) {

        // 팀원이 만든 유틸로 로그인한 유저 ID 검증 및 추출!
        Long userId = SecurityContextUtil.extractUserId(request);

        // 안전하게 획득한 userId와 세션 ID를 서비스 레이어로 전달
        PillUploadUrlsResponse result = s3PresignService.generateUploadUrls(
                presignUrlRequest.sessionId(),
                userId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        ResultCode.PRESIGNED_URL_ISSUED.name(),
                        ResultCode.PRESIGNED_URL_ISSUED.getMessage(),
                        result
                )
        );
    }


    @PostMapping("/identify")
    public ResponseEntity<ApiResponse<List<MedicationIdentifyResponse>>> identify(
            HttpServletRequest request,
            @Valid @RequestBody IdentifyRequest identifyRequest) {

        Long userId = SecurityContextUtil.extractUserId(request);
        List<MedicationIdentifyResponse> result = medicationIdentifyService.identify(userId, identifyRequest);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ResultCode.MEDICATION_ANALYSIS_SUCCESS.name(),
                        ResultCode.MEDICATION_ANALYSIS_SUCCESS.getMessage(),
                        result
                )
        );
    }
}
