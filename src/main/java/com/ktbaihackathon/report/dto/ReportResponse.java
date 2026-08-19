package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.entity.SymptomType;
import java.time.Instant;
import java.util.List;

public record ReportResponse(
    Long id,
    Long userId,
    String userName,
    List<SymptomType> symptoms,
    Instant symptomStartedAt,
    String timezoneId,
    String memo,
    Long drugBolId,
    String drugName,
    String drugImageUrl,
    Instant takenAt,
    ReportSnapshotStatus snapshotStatus,
    String snapshotObjectKey,
    String snapshotFileName,
    Long snapshotContentLength,
    Instant createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getId(),
            report.getUserId(),
            report.getUserName(),
            List.copyOf(report.getSymptoms()),
            report.getSymptomStartedAt(),
            report.getTimezoneId(),
            report.getMemo(),
            report.getDrugBolId(),
            report.getDrugName(),
            report.getDrugImageUrl(),
            report.getTakenAt(),
            report.getSnapshotStatus(),
            report.getSnapshotObjectKey(),
            report.getSnapshotFileName(),
            report.getSnapshotContentLength(),
            report.getCreatedAt()
        );
    }
}
