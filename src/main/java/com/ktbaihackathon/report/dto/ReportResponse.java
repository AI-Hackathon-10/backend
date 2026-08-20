package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.medication.entity.MedicationEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import com.ktbaihackathon.symptom.entity.SymptomType;

public record ReportResponse(
        Long reportId,
        Long symptomRecordId,
        String userName,
        List<SymptomType> symptomTypes,
        Instant startedAt,
        String memo,
        String summary,
        ReportSnapshotStatus snapshotStatus,
        String snapshotObjectKey,
        Instant createdAt,
        MedicationInfo medication
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getReportId(),
                report.getSymptomRecord().getSymptomRecordId(),
                report.getUser().getName(),
                report.getSymptomRecord().getSymptomMaps().stream()
                        .map(symptomMap -> symptomMap.getSymptomType())
                        .toList(),
                report.getSymptomRecord().getStartedAt(),
                report.getSymptomRecord().getMemo(),
                report.getSummary(),
                report.getSnapshotStatus(),
                report.getSnapshotObjectKey(),
                report.getCreatedAt(),
                MedicationInfo.from(report.getMedication())
        );
    }

    public record MedicationInfo(
            Long medicationId,
            String drugName,
            String frontImageUrl,
            String backImageUrl,
            LocalDateTime takenAt
    ) {

        private static MedicationInfo from(MedicationEntity medication) {
            return new MedicationInfo(
                    medication.getDrugRecognitionId(),
                    medication.getDrugName(),
                    medication.getFrontImageUrl(),
                    medication.getBackImageUrl(),
                    medication.getTakenAt()
            );
        }
    }
}
