package com.ktbaihackathon.medication.dto;

import com.ktbaihackathon.symptom.entity.SymptomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

public record IdentifyRequest(
        @NotBlank(message = "requestId는 필수입니다.")
        String requestId,
        @NotEmpty(message = "증상은 최소 1개 이상 선택해야 합니다.")
        @Size(max = 10, message = "증상은 최대 10개까지 선택할 수 있습니다.")
        List<@NotNull SymptomType> symptomTypes,
        @NotNull(message = "증상 시작 시점은 필수입니다.")
        OffsetDateTime startedAt
) {}
