package com.ktbaihackathon.report.controller;

import com.ktbaihackathon.common.exception.GlobalExceptionHandler;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.report.entity.Report;
import com.ktbaihackathon.report.entity.ReportSnapshotStatus;
import com.ktbaihackathon.report.dto.ReportResponse;
import com.ktbaihackathon.report.service.ReportService;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.entity.SymptomType;
import com.ktbaihackathon.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void createsReportFromRecordIdsForAuthenticatedUser() throws Exception {
        Report report = mockReport();
        when(reportService.create(eq(1L), any())).thenReturn(report);

        String requestBody = """
                {
                  "symptomRecordId": 5,
                  "medicationId": 3
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
                .andExpect(jsonPath("$.result.medication.frontImageUrl").value("images/front.jpg"))
                .andExpect(jsonPath("$.result.medication.backImageUrl").value("images/back.jpg"));

        verify(reportService).create(eq(1L), any());
    }

    @Test
    void rejectsMissingMedicationId() throws Exception {
        String requestBody = """
                {
                  "symptomRecordId": 5
                }
                """;

        mockMvc.perform(post("/api/reports")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getsAuthenticatedUsersReports() throws Exception {
        when(reportService.findAll(eq(1L))).thenReturn(List.of(mockReportResponse()));

        mockMvc.perform(get("/api/reports")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].reportId").value(20))
                .andExpect(jsonPath("$.result[0].symptomRecordId").value(5));

        verify(reportService).findAll(1L);
    }

    @Test
    void getsAuthenticatedUsersReportById() throws Exception {
        when(reportService.findOne(eq(1L), eq(20L))).thenReturn(mockReportResponse());

        mockMvc.perform(get("/api/reports/20")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.reportId").value(20))
                .andExpect(jsonPath("$.result.symptomRecordId").value(5));

        verify(reportService).findOne(1L, 20L);
    }

    private ReportResponse mockReportResponse() {
        return new ReportResponse(
                20L,
                5L,
                "홍길동",
                List.of(),
                Instant.parse("2026-08-20T01:00:00Z"),
                null,
                "증상: 두통, 발열",
                ReportSnapshotStatus.PENDING,
                null,
                Instant.parse("2026-08-20T02:00:00Z"),
                new ReportResponse.MedicationInfo(
                        3L,
                        null,
                        "https://example.com/front.jpg",
                        "https://example.com/back.jpg",
                        null
                )
        );
    }

    private Report mockReport() {
        Report report = mock(Report.class);
        User user = mock(User.class);
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        MedicationEntity medication = mock(MedicationEntity.class);

        when(report.getReportId()).thenReturn(20L);
        when(report.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("홍길동");
        when(report.getSymptomRecord()).thenReturn(symptomRecord);
        when(symptomRecord.getSymptomRecordId()).thenReturn(5L);
        when(symptomRecord.getSymptomMaps()).thenReturn(List.of());
        when(report.getMedication()).thenReturn(medication);
        when(medication.getMedicationRecognitionId()).thenReturn(3L);
        when(medication.getFrontImageObjectKey()).thenReturn("images/front.jpg");
        when(medication.getBackImageObjectKey()).thenReturn("images/back.jpg");
        when(report.getSummary()).thenReturn("증상: 두통, 발열\n시작 시점: 2026-08-20 01:00");
        when(report.getSnapshotStatus()).thenReturn(ReportSnapshotStatus.PENDING);
        when(report.getCreatedAt()).thenReturn(Instant.parse("2026-08-20T02:00:00Z"));

        return report;
    }
}
