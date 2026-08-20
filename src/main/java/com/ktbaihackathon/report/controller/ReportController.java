package com.ktbaihackathon.report.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.common.security.SecurityContextUtil;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotPresignedUrlResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUrlResponse;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.service.ReportService;
import com.ktbaihackathon.report.service.ReportSnapshotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ktbaihackathon.common.response.ResultCode.REPORT_CREATE_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.REPORT_QUERY_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.REPORT_SNAPSHOT_COMPLETE_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.REPORT_SNAPSHOT_PRESIGNED_URL_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.REPORT_SNAPSHOT_URL_SUCCESS;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportSnapshotService reportSnapshotService;

    @PostMapping
    public ApiResponse<ReportResponse> create(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);
        Report report = reportService.create(userId, request);

        return ApiResponse.success(
                REPORT_CREATE_SUCCESS.name(),
                REPORT_CREATE_SUCCESS.getMessage(),
                ReportResponse.from(report)
        );
    }

    @GetMapping
    public ApiResponse<List<ReportResponse>> findAll(
            HttpServletRequest servletRequest
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);

        return ApiResponse.success(
                REPORT_QUERY_SUCCESS.name(),
                REPORT_QUERY_SUCCESS.getMessage(),
                reportService.findAll(userId)
        );
    }

    @GetMapping("/{reportId}")
    public ApiResponse<ReportResponse> findOne(
            HttpServletRequest servletRequest,
            @PathVariable Long reportId
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);

        return ApiResponse.success(
                REPORT_QUERY_SUCCESS.name(),
                REPORT_QUERY_SUCCESS.getMessage(),
                reportService.findOne(userId, reportId)
        );
    }

    @PostMapping("/{reportId}/snapshot/presigned-url")
    public ApiResponse<ReportSnapshotPresignedUrlResponse> issueSnapshotUploadUrl(
            HttpServletRequest servletRequest,
            @PathVariable Long reportId
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);

        return ApiResponse.success(
                REPORT_SNAPSHOT_PRESIGNED_URL_SUCCESS.name(),
                REPORT_SNAPSHOT_PRESIGNED_URL_SUCCESS.getMessage(),
                reportSnapshotService.issueUploadUrl(userId, reportId)
        );
    }

    @PostMapping("/{reportId}/snapshot/complete")
    public ApiResponse<Void> completeSnapshotUpload(
            HttpServletRequest servletRequest,
            @PathVariable Long reportId
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);
        reportSnapshotService.completeUpload(userId, reportId);

        return ApiResponse.success(
                REPORT_SNAPSHOT_COMPLETE_SUCCESS.name(),
                REPORT_SNAPSHOT_COMPLETE_SUCCESS.getMessage()
        );
    }

    @GetMapping("/{reportId}/snapshot-url")
    public ApiResponse<ReportSnapshotUrlResponse> getSnapshotUrl(
            HttpServletRequest servletRequest,
            @PathVariable Long reportId
    ) {
        Long userId = SecurityContextUtil.extractUserId(servletRequest);

        return ApiResponse.success(
                REPORT_SNAPSHOT_URL_SUCCESS.name(),
                REPORT_SNAPSHOT_URL_SUCCESS.getMessage(),
                reportSnapshotService.getSnapshotUrl(userId, reportId)
        );
    }
}
