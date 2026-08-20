package com.ktbaihackathon.symptom.controller;

import com.ktbaihackathon.common.exception.GlobalExceptionHandler;
import com.ktbaihackathon.symptom.entity.SymptomMap;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.symptom.entity.SymptomType;
import com.ktbaihackathon.symptom.service.SymptomRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SymptomRecordControllerTest {

    private MockMvc mockMvc;
    private SymptomRecordService symptomRecordService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        symptomRecordService = mock(SymptomRecordService.class);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SymptomRecordController(symptomRecordService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsSymptomRecordForAuthenticatedUser() throws Exception {
        SymptomRecord symptomRecord = mockSymptomRecord();
        when(symptomRecordService.create(eq(1L), any())).thenReturn(symptomRecord);

        String requestBody = """
                {
                  "symptomTypes": ["HEADACHE", "FEVER"],
                  "startedAt": "2026-08-20T01:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/symptoms/records")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.symptomRecordId").value(10))
                .andExpect(jsonPath("$.result.symptomTypes[0]").value("두통"))
                .andExpect(jsonPath("$.result.symptomTypes[1]").value("발열"));

        verify(symptomRecordService).create(eq(1L), any());
    }

    @Test
    void rejectsMoreThanTwoSymptoms() throws Exception {
        String requestBody = """
                {
                  "symptomTypes": ["HEADACHE", "FEVER", "COUGH"],
                  "startedAt": "2026-08-20T01:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/symptoms/records")
                        .requestAttr("userId", 1L)
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private SymptomRecord mockSymptomRecord() {
        SymptomRecord symptomRecord = mock(SymptomRecord.class);
        SymptomMap headache = mock(SymptomMap.class);
        SymptomMap fever = mock(SymptomMap.class);

        when(symptomRecord.getSymptomRecordId()).thenReturn(10L);
        when(symptomRecord.getSymptomMaps()).thenReturn(List.of(headache, fever));
        when(symptomRecord.getStartedAt()).thenReturn(Instant.parse("2026-08-20T01:00:00Z"));
        when(headache.getSymptomType()).thenReturn(SymptomType.HEADACHE);
        when(fever.getSymptomType()).thenReturn(SymptomType.FEVER);

        return symptomRecord;
    }
}
