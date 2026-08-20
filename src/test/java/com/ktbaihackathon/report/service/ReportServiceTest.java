package com.ktbaihackathon.report.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.report.dto.ReportCreateRequest;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.repository.ReportRepository;
import com.ktbaihackathon.symptom.entity.SymptomMap;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.entity.SymptomType;
import com.ktbaihackathon.symptom.repository.SymptomRecordRepository;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {

    private final ReportRepository reportRepository = mock(ReportRepository.class);
    private final SymptomRecordRepository symptomRecordRepository = mock(SymptomRecordRepository.class);
    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ReportService reportService = new ReportService(
            reportRepository,
            symptomRecordRepository,
            medicationRepository,
            userRepository
    );

    @Test
    void createsSummaryFromStoredSymptomAndMedicationData() {
        User user = mock(User.class);
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        SymptomMap headache = mock(SymptomMap.class);
        SymptomMap fever = mock(SymptomMap.class);
        MedicationEntity medication = mock(MedicationEntity.class);
        LocalDateTime takenAt = LocalDateTime.of(2026, 8, 20, 2, 30);

        stubOwnedData(user, symptomRecord, medication);
        when(user.getName()).thenReturn("홍길동");
        when(symptomRecord.getStartedAt()).thenReturn(Instant.parse("2026-08-20T01:00:00Z"));
        when(symptomRecord.getMemo()).thenReturn("밤부터 머리가 아픕니다");
        when(symptomRecord.getSymptomMaps()).thenReturn(List.of(headache, fever));
        when(headache.getSymptomType()).thenReturn(SymptomType.HEADACHE);
        when(fever.getSymptomType()).thenReturn(SymptomType.FEVER);
        when(medication.isTaken()).thenReturn(true);
        when(medication.getTakenAt()).thenReturn(takenAt);
        when(medication.getDrugName()).thenReturn("타이레놀");
        when(medication.getFrontImageObjectKey()).thenReturn("uploads/front.jpg");
        when(medication.getBackImageObjectKey()).thenReturn("uploads/back.jpg");
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Report report = reportService.create(1L, new ReportCreateRequest(10L, 20L));

        assertThat(report.getSummary()).isEqualTo("""
                사용자: 홍길동
                증상: 두통, 발열
                증상 시작 시각: 2026-08-20T01:00:00Z
                메모: 밤부터 머리가 아픕니다
                복용 약품: 타이레놀
                복용 시각: 2026-08-20T02:30
                앞면 이미지: uploads/front.jpg
                뒷면 이미지: uploads/back.jpg
                """.stripTrailing());
    }

    @Test
    void rejectsReportCreationWhenMedicationIsNotConfirmed() {
        User user = mock(User.class);
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        MedicationEntity medication = mock(MedicationEntity.class);

        stubOwnedData(user, symptomRecord, medication);
        when(medication.isTaken()).thenReturn(false);

        assertThatThrownBy(() -> reportService.create(1L, new ReportCreateRequest(10L, 20L)))
                .isInstanceOfSatisfying(CustomException.class, exception ->
                        assertThat(exception.getResultCode())
                                .isEqualTo(ResultCode.REPORT_MEDICATION_NOT_TAKEN));

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void usesFallbackValuesWhenOptionalDocumentDataIsMissing() {
        User user = mock(User.class);
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        SymptomMap headache = mock(SymptomMap.class);
        MedicationEntity medication = mock(MedicationEntity.class);

        stubOwnedData(user, symptomRecord, medication);
        when(user.getName()).thenReturn("홍길동");
        when(symptomRecord.getStartedAt()).thenReturn(Instant.parse("2026-08-20T01:00:00Z"));
        when(symptomRecord.getMemo()).thenReturn(null);
        when(symptomRecord.getSymptomMaps()).thenReturn(List.of(headache));
        when(headache.getSymptomType()).thenReturn(SymptomType.HEADACHE);
        when(medication.isTaken()).thenReturn(true);
        when(medication.getTakenAt()).thenReturn(LocalDateTime.of(2026, 8, 20, 2, 30));
        when(medication.getDrugName()).thenReturn(null);
        when(medication.getFrontImageObjectKey()).thenReturn(null);
        when(medication.getBackImageObjectKey()).thenReturn(null);
        when(reportRepository.save(any(Report.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Report report = reportService.create(1L, new ReportCreateRequest(10L, 20L));

        assertThat(report.getSummary()).contains(
                "메모: 입력 없음",
                "복용 약품: 판별되지 않음",
                "앞면 이미지: 없음",
                "뒷면 이미지: 없음"
        );
    }

    private void stubOwnedData(
            User user,
            SymptomRecord symptomRecord,
            MedicationEntity medication
    ) {
        when(user.getUserId()).thenReturn(1L);
        when(symptomRecord.getUser()).thenReturn(user);
        when(medication.getUser()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(symptomRecordRepository.findById(10L)).thenReturn(Optional.of(symptomRecord));
        when(medicationRepository.findById(20L)).thenReturn(Optional.of(medication));
    }
}
