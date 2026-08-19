package com.ktbaihackathon.symptom.dto;

import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.entity.SymptomType;

import java.time.Instant;
import java.util.List;

public record SymptomRecordResponse(
        Long symptomRecordId,
        List<SymptomType> symptomTypes,
        Instant startedAt,
        String memo,
        Instant createdAt
) {

    public static SymptomRecordResponse from(SymptomRecord symptomRecord) {
        List<SymptomType> symptomTypes = symptomRecord.getSymptomMaps().stream()
                .map(symptomMap -> symptomMap.getSymptomType())
                .toList();

        return new SymptomRecordResponse(
                symptomRecord.getSymptomRecordId(),
                symptomTypes,
                symptomRecord.getStartedAt(),
                symptomRecord.getMemo(),
                symptomRecord.getCreatedAt()
        );
    }
}
