package com.ktbaihackathon.report.service;

import com.ktbaihackathon.config.s3.S3Properties;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.dto.ReportCardResponse;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.dto.ReportSnapshotUploadUrlResponse;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.exception.InvalidReportRequestException;
import com.ktbaihackathon.report.exception.ReportNotFoundException;
import com.ktbaihackathon.report.repository.ReportRepository;
import com.ktbaihackathon.report.storage.PresignedUpload;
import com.ktbaihackathon.report.storage.ReportSnapshotStorage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportSnapshotStorage reportSnapshotStorage;
    private final S3Properties s3Properties;

    @Transactional
    public ReportResponse create(ReportCreateRequest request) {
        validate(request);

        Report report = Report.create(
            request.userId(),
            request.userName().trim(),
            request.symptoms(),
            request.symptomStartedAt(),
            request.timezoneId().trim(),
            request.memo() == null ? "" : request.memo().trim(),
            request.drugBolId(),
            request.drugName().trim(),
            request.drugImageUrl().trim(),
            request.takenAt()
        );

        return ReportResponse.from(reportRepository.save(report));
    }

    @Transactional
    public ReportSnapshotUploadUrlResponse createSnapshotUploadUrl(Long reportId, String contentType) {
        if (!"image/png".equalsIgnoreCase(contentType)) {
            throw new InvalidReportRequestException("문서 스냅샷은 PNG 파일이어야 합니다.");
        }

        Report report = findReport(reportId);
        if (report.getSnapshotStatus() == ReportSnapshotStatus.UPLOADED) {
            throw new InvalidReportRequestException("이미 업로드된 문서 스냅샷은 변경할 수 없습니다.");
        }

        String fileName = createSnapshotFileName(report.getUserName(), report.getId());
        String objectKey = createObjectKey(report.getUserId(), fileName);
        PresignedUpload upload = reportSnapshotStorage.createUploadUrl(objectKey, s3Properties.getPresignedUrlExpiration());
        report.prepareSnapshotUpload(objectKey, fileName);

        return ReportSnapshotUploadUrlResponse.from(upload, objectKey, fileName);
    }

    @Transactional
    public ReportResponse completeSnapshotUpload(Long reportId) {
        Report report = findReport(reportId);
        if (report.getSnapshotObjectKey() == null) {
            throw new InvalidReportRequestException("문서 스냅샷 업로드 URL이 발급되지 않았습니다.");
        }

        long contentLength = reportSnapshotStorage.verifyPng(
            report.getSnapshotObjectKey(),
            s3Properties.getMaxReportSizeBytes()
        );
        report.completeSnapshotUpload(contentLength);

        return ReportResponse.from(report);
    }

    public ReportResponse getReport(Long reportId) {
        return ReportResponse.from(findReport(reportId));
    }

    public List<ReportCardResponse> getReportCards(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidReportRequestException("사용자 정보가 올바르지 않습니다.");
        }

        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(report -> ReportCardResponse.from(report, createSnapshotViewUrl(report)))
            .toList();
    }

    private void validate(ReportCreateRequest request) {
        if (request == null) {
            throw new InvalidReportRequestException("리포트 정보가 필요합니다.");
        }
        if (request.symptoms() == null || request.symptoms().isEmpty() || request.symptoms().size() > 20) {
            throw new InvalidReportRequestException("증상은 1개 이상 20개 이하로 입력해야 합니다.");
        }
        if (request.symptoms().stream().anyMatch(symptom -> symptom == null)) {
            throw new InvalidReportRequestException("증상 항목은 비어 있을 수 없습니다.");
        }
        if (isBlank(request.userName()) || isBlank(request.drugName())) {
            throw new InvalidReportRequestException("사용자명과 약 이름은 필수입니다.");
        }
        if (request.memo() != null && request.memo().trim().length() > 200) {
            throw new InvalidReportRequestException("메모는 200자 이하여야 합니다.");
        }
        if (request.symptomStartedAt() == null || request.takenAt() == null) {
            throw new InvalidReportRequestException("증상 시작 시각과 복용 시각은 필수입니다.");
        }
        Instant now = Instant.now();
        if (request.symptomStartedAt().isAfter(now) || request.takenAt().isAfter(now)) {
            throw new InvalidReportRequestException("증상 시작 시각과 복용 시각은 미래일 수 없습니다.");
        }
        validateTimezone(request.timezoneId());
        validateImageUrl(request.drugImageUrl());
    }

    private void validateTimezone(String timezoneId) {
        if (isBlank(timezoneId)) {
            throw new InvalidReportRequestException("시간대 정보가 필요합니다.");
        }
        try {
            ZoneId.of(timezoneId);
        } catch (RuntimeException exception) {
            throw new InvalidReportRequestException("유효하지 않은 시간대입니다.");
        }
    }

    private void validateImageUrl(String imageUrl) {
        if (isBlank(imageUrl)) {
            throw new InvalidReportRequestException("낱알 이미지 URL이 필요합니다.");
        }
        try {
            URI uri = URI.create(imageUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new InvalidReportRequestException("낱알 이미지 URL은 HTTP(S) URL이어야 합니다.");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidReportRequestException("유효하지 않은 낱알 이미지 URL입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Report findReport(Long reportId) {
        return reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));
    }

    private String createSnapshotFileName(String userName, Long reportId) {
        String safeUserName = userName.trim()
            .replaceAll("[\\\\/:*?\\\"<>|\\p{Cntrl}]", "_")
            .replaceAll("\\s+", "_");
        return safeUserName + "_" + reportId + ".png";
    }

    private String createObjectKey(Long userId, String fileName) {
        String prefix = s3Properties.getReportPrefix().replaceAll("^/+|/+$", "");
        return prefix + "/" + userId + "/" + fileName;
    }

    private String createSnapshotViewUrl(Report report) {
        if (report.getSnapshotStatus() != ReportSnapshotStatus.UPLOADED) {
            return null;
        }
        return reportSnapshotStorage.createViewUrl(
            report.getSnapshotObjectKey(),
            s3Properties.getPresignedUrlExpiration()
        );
    }
}
