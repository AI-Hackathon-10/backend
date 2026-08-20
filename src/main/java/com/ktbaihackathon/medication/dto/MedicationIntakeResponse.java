package com.ktbaihackathon.medication.dto;

import com.ktbaihackathon.medication.entity.MedicationEntity;

import java.time.LocalDateTime;

public record MedicationIntakeResponse(
        Long medicationId,
        String drugName,
        LocalDateTime takenAt
) {

    public static MedicationIntakeResponse from(MedicationEntity medication) {
        return new MedicationIntakeResponse(
                medication.getMedicationRecognitionId(),
                medication.getDrugName(),
                medication.getTakenAt()
        );
    }
}
