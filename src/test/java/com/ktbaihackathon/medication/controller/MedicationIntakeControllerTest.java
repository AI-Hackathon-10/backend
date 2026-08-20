package com.ktbaihackathon.medication.controller;

import com.ktbaihackathon.common.exception.GlobalExceptionHandler;
import com.ktbaihackathon.medication.dto.MedicationIntakeResponse;
import com.ktbaihackathon.medication.service.MedicationIntakeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicationIntakeControllerTest {

    private MockMvc mockMvc;
    private MedicationIntakeService medicationIntakeService;

    @BeforeEach
    void setUp() {
        medicationIntakeService = mock(MedicationIntakeService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new MedicationIntakeController(medicationIntakeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void confirmsMedicationIntakeForAuthenticatedUser() throws Exception {
        when(medicationIntakeService.markAsTaken(eq(1L), eq(7L)))
                .thenReturn(new MedicationIntakeResponse(
                        7L,
                        "타이레놀",
                        LocalDateTime.of(2026, 8, 20, 2, 30)
                ));

        mockMvc.perform(post("/api/medications/7/intake")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.medicationId").value(7))
                .andExpect(jsonPath("$.result.drugName").value("타이레놀"))
                .andExpect(jsonPath("$.result.takenAt").value("2026-08-20T02:30:00"));
    }
}
