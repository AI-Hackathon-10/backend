package com.ktbaihackathon.medication.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.PillUploadUrlsResponse;
import com.ktbaihackathon.medication.dto.PresignUrlRequest;
import com.ktbaihackathon.medication.service.S3PresignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medications/upload")
@RequiredArgsConstructor
public class MedicationUploadController {

    private final S3PresignService s3PresignService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PillUploadUrlsResponse>> getPresignedUrls(
            @RequestBody PresignUrlRequest request) {

        PillUploadUrlsResponse result = s3PresignService.generateUploadUrls(request.sessionId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        ResultCode.PRESIGNED_URL_ISSUED.name(),
                        ResultCode.PRESIGNED_URL_ISSUED.getMessage(),
                        result
                )
        );
    }
}