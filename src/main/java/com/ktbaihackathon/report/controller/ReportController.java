package com.ktbaihackathon.report.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.report.dto.ReportCardResponse;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUploadUrlRequest;
import com.ktbaihackathon.report.dto.ReportSnapshotUploadUrlResponse;
import com.ktbaihackathon.report.service.ReportService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
        @Valid @RequestBody ReportCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.of(reportService.create(request)));
    }

    @PostMapping("/reports/{reportId}/snapshot-upload-url")
    public ApiResponse<ReportSnapshotUploadUrlResponse> createSnapshotUploadUrl(
        @PathVariable Long reportId,
        @Valid @RequestBody ReportSnapshotUploadUrlRequest request
    ) {
        return ApiResponse.of(reportService.createSnapshotUploadUrl(reportId, request.contentType()));
    }

    @PostMapping("/reports/{reportId}/snapshot-upload-complete")
    public ApiResponse<ReportResponse> completeSnapshotUpload(@PathVariable Long reportId) {
        return ApiResponse.of(reportService.completeSnapshotUpload(reportId));
    }

    @GetMapping("/reports/{reportId}")
    public ApiResponse<ReportResponse> getReport(@PathVariable Long reportId) {
        return ApiResponse.of(reportService.getReport(reportId));
    }

    @GetMapping("/users/{userId}/reports")
    public ApiResponse<List<ReportCardResponse>> getUserReports(@PathVariable Long userId) {
        return ApiResponse.of(reportService.getReportCards(userId));
    }
}
