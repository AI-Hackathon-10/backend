package com.ktbaihackathon.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ktbaihackathon.config.JpaAuditingConfig;
import com.ktbaihackathon.config.s3.S3Properties;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUploadUrlResponse;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.entity.SymptomType;
import com.ktbaihackathon.report.exception.InvalidReportRequestException;
import com.ktbaihackathon.report.storage.PresignedUpload;
import com.ktbaihackathon.report.storage.ReportSnapshotStorage;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, ReportService.class, ReportServiceTest.StorageConfig.class})
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Test
    void createsPendingReportFromImmutableSymptomAndMedicationSnapshots() {
        ReportResponse response = reportService.create(validRequest());

        assertThat(response.id()).isNotNull();
        assertThat(response.userName()).isEqualTo("홍길동");
        assertThat(response.symptoms()).containsExactly(SymptomType.HEADACHE, SymptomType.FEVER);
        assertThat(response.drugName()).isEqualTo("타이레놀정");
        assertThat(response.snapshotStatus()).isEqualTo(ReportSnapshotStatus.PENDING);
        assertThat(response.snapshotObjectKey()).isNull();
    }

    @Test
    void rejectsFutureMedicationTime() {
        ReportCreateRequest request = new ReportCreateRequest(
            7L,
            "홍길동",
            List.of(SymptomType.HEADACHE),
            Instant.parse("2025-08-19T11:00:00Z"),
            "Asia/Seoul",
            "밤부터 두통과 미열이 있습니다.",
            15L,
            "타이레놀정",
            "https://example.com/images/tylenol.png",
            Instant.now().plusSeconds(60)
        );

        assertThatThrownBy(() -> reportService.create(request))
            .isInstanceOf(InvalidReportRequestException.class)
            .hasMessageContaining("복용 시각");
    }

    @Test
    void issuesPngUploadUrlWithTheRequiredReportFileName() {
        ReportResponse report = reportService.create(validRequest());

        ReportSnapshotUploadUrlResponse response = reportService.createSnapshotUploadUrl(report.id(), "image/png");

        String expectedFileName = "홍길동_" + report.id() + ".png";
        assertThat(response.fileName()).isEqualTo(expectedFileName);
        assertThat(response.objectKey()).isEqualTo("reports/7/" + expectedFileName);
        assertThat(response.contentType()).isEqualTo("image/png");
        assertThat(response.uploadUrl()).isEqualTo("https://upload.example.com/reports/7/" + expectedFileName);
    }

    @Test
    void marksReportUploadedOnlyAfterStorageVerificationSucceeds() {
        ReportResponse report = reportService.create(validRequest());
        reportService.createSnapshotUploadUrl(report.id(), "image/png");

        ReportResponse response = reportService.completeSnapshotUpload(report.id());

        assertThat(response.snapshotStatus()).isEqualTo(ReportSnapshotStatus.UPLOADED);
        assertThat(response.snapshotContentLength()).isEqualTo(512L);
    }

    private ReportCreateRequest validRequest() {
        return new ReportCreateRequest(
            7L,
            "홍길동",
            List.of(SymptomType.HEADACHE, SymptomType.FEVER),
            Instant.parse("2025-08-19T11:00:00Z"),
            "Asia/Seoul",
            "밤부터 두통과 미열이 있습니다.",
            15L,
            "타이레놀정",
            "https://example.com/images/tylenol.png",
            Instant.parse("2025-08-19T13:30:00Z")
        );
    }

    @TestConfiguration
    static class StorageConfig {

        @Bean
        S3Properties s3Properties() {
            S3Properties properties = new S3Properties();
            properties.setReportPrefix("reports");
            properties.setPresignedUrlExpirationSeconds(600);
            properties.setMaxReportSizeBytes(10 * 1024 * 1024L);
            return properties;
        }

        @Bean
        ReportSnapshotStorage reportSnapshotStorage() {
            return new ReportSnapshotStorage() {
                @Override
                public PresignedUpload createUploadUrl(String objectKey, java.time.Duration expiration) {
                    return new PresignedUpload(
                        "https://upload.example.com/" + objectKey,
                        Instant.parse("2025-08-19T14:00:00Z")
                    );
                }

                @Override
                public long verifyPng(String objectKey, long maximumSizeBytes) {
                    return 512L;
                }

                @Override
                public String createViewUrl(String objectKey, java.time.Duration expiration) {
                    return "https://view.example.com/" + objectKey;
                }
            };
        }
    }
}
