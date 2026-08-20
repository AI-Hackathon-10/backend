package com.ktbaihackathon.report.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.report.dto.ReportSnapshotPresignedUrlResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUrlResponse;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportSnapshotServiceTest {

    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final ReportSnapshotService reportSnapshotService = new ReportSnapshotService(
            s3Presigner,
            reportRepository,
            "pillid-images-team10"
    );

    @Test
    void issuesPngUploadUrlAndStoresObjectKeyForOwnedReport() throws Exception {
        Report report = mock(Report.class);
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);

        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.of(report));
        when(presignedRequest.url())
                .thenReturn(URI.create("https://s3.example/upload").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        ReportSnapshotPresignedUrlResponse response =
                reportSnapshotService.issueUploadUrl(1L, 20L);

        assertThat(response.reportId()).isEqualTo(20L);
        assertThat(response.objectKey())
                .isEqualTo("reports/1/20/snapshot.png");
        assertThat(response.uploadUrl()).isEqualTo("https://s3.example/upload");
        assertThat(response.expiresInSeconds()).isEqualTo(300L);

        verify(report).prepareSnapshotUpload("reports/1/20/snapshot.png");
        verify(reportRepository).save(report);

        var requestCaptor =
                org.mockito.ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().putObjectRequest().bucket())
                .isEqualTo("pillid-images-team10");
        assertThat(requestCaptor.getValue().putObjectRequest().key())
                .isEqualTo("reports/1/20/snapshot.png");
        assertThat(requestCaptor.getValue().putObjectRequest().contentType())
                .isEqualTo("image/png");
    }

    @Test
    void completesSnapshotUploadForOwnedReport() {
        Report report = mock(Report.class);

        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.of(report));
        when(report.getSnapshotObjectKey()).thenReturn("reports/1/20/snapshot.png");

        reportSnapshotService.completeUpload(1L, 20L);

        verify(report).completeSnapshotUpload();
        verify(reportRepository).save(report);
    }

    @Test
    void rejectsCompletingSnapshotBeforeUploadUrlWasIssued() {
        Report report = mock(Report.class);

        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.of(report));
        when(report.getSnapshotObjectKey()).thenReturn(null);

        assertThatThrownBy(() -> reportSnapshotService.completeUpload(1L, 20L))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.REPORT_SNAPSHOT_NOT_AVAILABLE));

        verify(report, never()).completeSnapshotUpload();
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void issuesDownloadUrlOnlyAfterSnapshotUploadCompletes() throws Exception {
        Report report = mock(Report.class);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);

        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.of(report));
        when(report.getSnapshotStatus()).thenReturn(ReportSnapshotStatus.UPLOADED);
        when(report.getSnapshotObjectKey()).thenReturn("reports/1/20/snapshot.png");
        when(presignedRequest.url())
                .thenReturn(URI.create("https://s3.example/download").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(presignedRequest);

        ReportSnapshotUrlResponse response =
                reportSnapshotService.getSnapshotUrl(1L, 20L);

        assertThat(response.reportId()).isEqualTo(20L);
        assertThat(response.snapshotUrl()).isEqualTo("https://s3.example/download");
        assertThat(response.expiresInSeconds()).isEqualTo(300L);
    }

    @Test
    void rejectsSnapshotUrlBeforeUploadCompletes() {
        Report report = mock(Report.class);

        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.of(report));
        when(report.getSnapshotStatus()).thenReturn(ReportSnapshotStatus.PENDING);

        assertThatThrownBy(() -> reportSnapshotService.getSnapshotUrl(1L, 20L))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.REPORT_SNAPSHOT_NOT_AVAILABLE));

        verifyNoInteractions(s3Presigner);
    }

    @Test
    void rejectsSnapshotOperationForMissingOrForeignReport() {
        when(reportRepository.findByReportIdAndUser_UserId(20L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportSnapshotService.issueUploadUrl(1L, 20L))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.INVALID_REQUEST));

        verifyNoInteractions(s3Presigner);
    }
}
