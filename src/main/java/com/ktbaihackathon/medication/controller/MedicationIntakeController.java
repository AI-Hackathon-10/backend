package com.ktbaihackathon.medication.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.common.security.SecurityContextUtil;
import com.ktbaihackathon.medication.dto.MedicationIntakeResponse;
import com.ktbaihackathon.medication.service.MedicationIntakeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
public class MedicationIntakeController {

    private final MedicationIntakeService medicationIntakeService;

    @PostMapping("/{medicationId}/intake")
    public ApiResponse<MedicationIntakeResponse> markAsTaken(
            HttpServletRequest servletRequest,
            @PathVariable Long medicationId
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);
        MedicationIntakeResponse response = medicationIntakeService.markAsTaken(userId, medicationId);

        return ApiResponse.success(
                ResultCode.MEDICATION_INTAKE_SUCCESS.name(),
                ResultCode.MEDICATION_INTAKE_SUCCESS.getMessage(),
                response
        );
    }
}
