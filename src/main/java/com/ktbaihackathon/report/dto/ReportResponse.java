package com.ktbaihackathon.report.dto;

import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;

import java.time.Instant;

public record ReportResponse(
        Long reportId,
        Long symptomRecordId,
        String summary,
        ReportSnapshotStatus snapshotStatus,
        Instant createdAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getReportId(),
                report.getSymptomRecord().getSymptomRecordId(),
                report.getSummary(),
                report.getSnapshotStatus(),
                report.getCreatedAt()
        );
    }
}
