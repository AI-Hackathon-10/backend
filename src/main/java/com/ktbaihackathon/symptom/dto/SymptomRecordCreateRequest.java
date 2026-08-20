package com.ktbaihackathon.symptom.dto;

import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.entity.SymptomType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record SymptomRecordCreateRequest(
        @NotEmpty(message = "증상은 최소 1개 이상 선택해야 합니다.")
        @Size(max = SymptomRecord.MAX_SYMPTOM_COUNT, message = "증상은 최대 10개까지 선택할 수 있습니다.")
        List<@NotNull SymptomType> symptomTypes,

        @NotNull(message = "증상 시작 시점은 필수입니다.")
        Instant startedAt,

        String memo
) {
}
