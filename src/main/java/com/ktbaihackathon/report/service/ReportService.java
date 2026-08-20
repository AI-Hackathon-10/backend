package com.ktbaihackathon.report.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
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

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final SymptomRecordRepository symptomRecordRepository;
    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;

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

        Report report = Report.create(
                user,
                symptomRecord,
                medication,
                request.summary()
        );

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> findAll(Long userId) {
        return reportRepository.findAllByUser_UserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReportResponse findOne(Long userId, Long reportId) {
        Report report = reportRepository.findByReportIdAndUser_UserId(reportId, userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_REQUEST));

        return ReportResponse.from(report);
    }

}
