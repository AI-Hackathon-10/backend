package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.entity.SymptomType;
import java.time.Instant;
import java.util.List;

public record ReportCardResponse(
    Long id,
    String userName,
    List<SymptomType> symptoms,
    String drugName,
    Instant takenAt,
    ReportSnapshotStatus snapshotStatus,
    String snapshotViewUrl,
    Instant createdAt
) {

    public static ReportCardResponse from(Report report, String snapshotViewUrl) {
        return new ReportCardResponse(
            report.getId(),
            report.getUserName(),
            List.copyOf(report.getSymptoms()),
            report.getDrugName(),
            report.getTakenAt(),
            report.getSnapshotStatus(),
            snapshotViewUrl,
            report.getCreatedAt()
        );
    }
}
