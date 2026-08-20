package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.medication.entity.MedicationEntity;

import java.time.Instant;
import java.time.LocalDateTime;

public record ReportResponse(
        Long reportId,
        Long symptomRecordId,
        String summary,
        ReportSnapshotStatus snapshotStatus,
        Instant createdAt,
        MedicationInfo medication
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getReportId(),
                report.getSymptomRecord().getSymptomRecordId(),
                report.getSummary(),
                report.getSnapshotStatus(),
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
