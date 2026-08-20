package com.ktbaihackathon.report.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.medication.service.S3ImageUrlService;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.repository.ReportRepository;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.repository.SymptomRecordRepository;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final SymptomRecordRepository symptomRecordRepository;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final S3ImageUrlService s3ImageUrlService;

    @Transactional
    public Report create(Long userId, ReportCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_TOKEN));

        SymptomRecord symptomRecord = symptomRecordRepository
                .findById(request.symptomRecordId())
                .filter(record -> record.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_REQUEST));

        MedicationEntity medication = medicationRepository
                .findById(request.medicationId())
                .filter(item -> item.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_REQUEST));

        if (!medication.isTaken() || medication.getTakenAt() == null) {
            throw new CustomException(ResultCode.REPORT_MEDICATION_NOT_TAKEN);
        }

        String summary = buildSummary(user, symptomRecord, medication);

        Report report = Report.create(
                user,
                symptomRecord,
                medication,
                summary
        );

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findAll(Long userId) {
        return reportRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReportResponse::from)
                .toList();
    }

    private String buildSummary(
            User user,
            SymptomRecord symptomRecord,
            MedicationEntity medication
    ) {
        String symptoms = symptomRecord.getSymptomMaps().stream()
                .map(symptomMap -> symptomMap.getSymptomType().getDisplayName())
                .collect(Collectors.joining(", "));

        return String.join(
                "\n",
                "사용자: " + formatOrFallback(user.getName(), "이름 없음"),
                "증상: " + formatOrFallback(symptoms, "없음"),
                "증상 시작 시각: " + formatOrFallback(
                        symptomRecord.getStartedAt(),
                        "입력 없음"
                ),
                "메모: " + formatOrFallback(symptomRecord.getMemo(), "입력 없음"),
                "복용 약품: " + formatOrFallback(medication.getDrugName(), "판별되지 않음"),
                "복용 시각: " + formatOrFallback(medication.getTakenAt(), "입력 없음"),
                "앞면 이미지: " + formatOrFallback(
                        medication.getFrontImageObjectKey(),
                        "없음"
                ),
                "뒷면 이미지: " + formatOrFallback(
                        medication.getBackImageObjectKey(),
                        "없음"
                )
        );
    }

    private String formatOrFallback(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }

        String text = value.toString();
        return text.isBlank() ? fallback : text;
    }

    @Transactional(readOnly = true)
    public ReportResponse findOne(Long userId, Long reportId) {
        Report report = reportRepository.findByReportIdAndUser_UserId(reportId, userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_REQUEST));
        MedicationEntity medication = report.getMedication();

        if (medication == null) {
            return ReportResponse.from(report);
        }

        String frontImageUrl = s3ImageUrlService.issueDownloadUrl(
                medication.getFrontImageObjectKey()
        );
        String backImageUrl = s3ImageUrlService.issueDownloadUrl(
                medication.getBackImageObjectKey()
        );

        return ReportResponse.from(report, frontImageUrl, backImageUrl);
    }

}
