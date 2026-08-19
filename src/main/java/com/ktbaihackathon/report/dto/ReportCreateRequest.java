package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.SymptomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record ReportCreateRequest(
    @NotNull @Positive Long userId,
    @NotBlank @Size(max = 50) String userName,
    @NotEmpty @Size(max = 20) List<@NotNull SymptomType> symptoms,
    @NotNull Instant symptomStartedAt,
    @NotBlank @Size(max = 40) String timezoneId,
    @Size(max = 200) String memo,
    @NotNull @Positive Long drugBolId,
    @NotBlank @Size(max = 100) String drugName,
    @NotBlank @Size(max = 2048) String drugImageUrl,
    @NotNull Instant takenAt
) {
}
