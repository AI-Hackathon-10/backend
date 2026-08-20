package com.ktbaihackathon.medication.controller;

import com.ktbaihackathon.common.exception.GlobalExceptionHandler;
import com.ktbaihackathon.medication.dto.MedicationIdentifyResponse;
import com.ktbaihackathon.medication.service.MedicationIdentifyService;
import com.ktbaihackathon.medication.service.S3PresignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicationUploadControllerTest {

    private MockMvc mockMvc;
    private MedicationIdentifyService medicationIdentifyService;

    @BeforeEach
    void setUp() {
        medicationIdentifyService = mock(MedicationIdentifyService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MedicationUploadController(mock(S3PresignService.class), medicationIdentifyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsMappedMedicationResultsAsArray() throws Exception {
        MedicationIdentifyResponse result = new MedicationIdentifyResponse(
                true,
                "199703222",
                "타이레놀정500밀리그람",
                "https://example.com/pill.jpg",
                new MedicationIdentifyResponse.Identification("HIGH", 1.0),
                new MedicationIdentifyResponse.Recommendation(
                        "RECOMMENDED", 0.94, "HIGH", "증상과 효능이 일치합니다.", "중복 복용 주의"),
                new MedicationIdentifyResponse.Features("TYLENOL", "500", "OBLONG", "WHITE", true),
                new MedicationIdentifyResponse.Official(
                        "199703222", "타이레놀정500밀리그람", "해열 및 진통", "용법 안내",
                        "경고", "주의", "상호작용", "부작용", "보관 방법", "https://example.com/pill.jpg"),
                "document"
        );
        when(medicationIdentifyService.identify(eq(1L), any())).thenReturn(List.of(result));

        mockMvc.perform(post("/api/medications/identify")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content("""
                                {
                                  "requestId": "request-1",
                                  "symptomTypes": ["HEADACHE"],
                                  "startedAt": "2026-08-20T01:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].itemName").value("타이레놀정500밀리그람"))
                .andExpect(jsonPath("$.result[0].identification.score").value(1.0))
                .andExpect(jsonPath("$.result[0].recommendation.status").value("RECOMMENDED"))
                .andExpect(jsonPath("$.result[0].official.efficacy").value("해열 및 진통"));
    }

    @Test
    void rejectsMissingSymptomInputs() throws Exception {
        mockMvc.perform(post("/api/medications/identify")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content("{\"requestId\":\"request-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
