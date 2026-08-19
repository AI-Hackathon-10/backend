package com.ktbaihackathon.symptom.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.security.SecurityContextUtil;
import com.ktbaihackathon.symptom.dto.SymptomRecordCreateRequest;
import com.ktbaihackathon.symptom.dto.SymptomRecordResponse;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.service.SymptomRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ktbaihackathon.common.response.ResultCode.SYMPTOM_RECORD_CREATE_SUCCESS;

@RestController
@RequestMapping("/api/symptoms/records")
@RequiredArgsConstructor
public class SymptomRecordController {

    private final SymptomRecordService symptomRecordService;

    @PostMapping
    public ApiResponse<SymptomRecordResponse> create(
            HttpServletRequest servletRequest,
            @Valid @RequestBody SymptomRecordCreateRequest request
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);
        SymptomRecord symptomRecord = symptomRecordService.create(userId, request);

        return ApiResponse.success(
                SYMPTOM_RECORD_CREATE_SUCCESS.name(),
                SYMPTOM_RECORD_CREATE_SUCCESS.getMessage(),
                SymptomRecordResponse.from(symptomRecord)
        );
    }
}
