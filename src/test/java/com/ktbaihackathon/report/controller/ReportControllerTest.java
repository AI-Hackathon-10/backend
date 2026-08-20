package com.ktbaihackathon.report.controller;

import com.ktbaihackathon.common.exception.GlobalExceptionHandler;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.service.ReportService;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

    private MockMvc mockMvc;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReportController(reportService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsReportForAuthenticatedUser() throws Exception {
        Report report = mockReport();
        when(reportService.create(eq(1L), any())).thenReturn(report);

        String requestBody = """
                {
                  "symptomRecordId": 5,
                  "medicationId": 3,
                  "summary": "증상: 두통, 발열\\n시작 시점: 2026-08-20 01:00"
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.reportId").value(20))
                .andExpect(jsonPath("$.result.symptomRecordId").value(5))
                .andExpect(jsonPath("$.result.summary").value("증상: 두통, 발열\n시작 시점: 2026-08-20 01:00"))
                .andExpect(jsonPath("$.result.snapshotStatus").value("PENDING"))
                .andExpect(jsonPath("$.result.medication.medicationId").value(3))
                .andExpect(jsonPath("$.result.medication.drugName").doesNotExist())
                .andExpect(jsonPath("$.result.medication.frontImageUrl").value("https://example.com/front.jpg"))
                .andExpect(jsonPath("$.result.medication.backImageUrl").value("https://example.com/back.jpg"));

        verify(reportService).create(eq(1L), any());
    }

    @Test
    void rejectsBlankSummary() throws Exception {
        String requestBody = """
                {
                  "symptomRecordId": 5,
                  "medicationId": 3,
                  "summary": " "
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Report mockReport() {
        Report report = mock(Report.class);
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        MedicationEntity medication = mock(MedicationEntity.class);

        when(report.getReportId()).thenReturn(20L);
        when(report.getSymptomRecord()).thenReturn(symptomRecord);
        when(symptomRecord.getSymptomRecordId()).thenReturn(5L);
        when(report.getMedication()).thenReturn(medication);
        when(medication.getDrugRecognitionId()).thenReturn(3L);
        when(medication.getFrontImageUrl()).thenReturn("https://example.com/front.jpg");
        when(medication.getBackImageUrl()).thenReturn("https://example.com/back.jpg");
        when(report.getSummary()).thenReturn("증상: 두통, 발열\n시작 시점: 2026-08-20 01:00");
        when(report.getSnapshotStatus()).thenReturn(ReportSnapshotStatus.PENDING);
        when(report.getCreatedAt()).thenReturn(Instant.parse("2026-08-20T02:00:00Z"));

        return report;
    }
}
