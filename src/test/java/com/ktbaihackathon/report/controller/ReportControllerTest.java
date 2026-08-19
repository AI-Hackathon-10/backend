package com.ktbaihackathon.report.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ktbaihackathon.report.storage.PresignedUpload;
import com.ktbaihackathon.report.storage.ReportSnapshotStorage;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(ReportControllerTest.StorageConfig.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsReportWithKoreanEnumSymptomsInResultResponse() throws Exception {
        mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJson()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.result.userName").value("홍길동"))
            .andExpect(jsonPath("$.result.symptoms[0]").value("두통"))
            .andExpect(jsonPath("$.result.symptoms[1]").value("발열"))
            .andExpect(jsonPath("$.result.snapshotStatus").value("PENDING"));
    }

    @Test
    void rejectsNonPngSnapshotUploadUrlRequests() throws Exception {
        long reportId = createReport();

        mockMvc.perform(post("/api/v1/reports/{reportId}/snapshot-upload-url", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentType\":\"image/jpeg\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_REPORT_REQUEST"));
    }

    @Test
    void returnsUploadedReportCardsWithPresignedViewUrl() throws Exception {
        long reportId = createReport();

        mockMvc.perform(post("/api/v1/reports/{reportId}/snapshot-upload-url", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"contentType\":\"image/png\"}"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reports/{reportId}/snapshot-upload-complete", reportId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.snapshotStatus").value("UPLOADED"));

        mockMvc.perform(get("/api/v1/users/{userId}/reports", 7L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result[0].id").value(reportId))
            .andExpect(jsonPath("$.result[0].snapshotViewUrl")
                .value("https://view.example.com/reports/7/홍길동_" + reportId + ".png"));
    }

    private long createReport() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreateJson()))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("result").path("id").asLong();
    }

    private String validCreateJson() {
        return """
            {
              "userId": 7,
              "userName": "홍길동",
              "symptoms": ["두통", "발열"],
              "symptomStartedAt": "2025-08-19T11:00:00Z",
              "timezoneId": "Asia/Seoul",
              "memo": "밤부터 두통과 미열이 있습니다.",
              "drugBolId": 15,
              "drugName": "타이레놀정",
              "drugImageUrl": "https://example.com/images/tylenol.png",
              "takenAt": "2025-08-19T13:30:00Z"
            }
            """;
    }

    @TestConfiguration
    static class StorageConfig {

        @Bean
        @Primary
        ReportSnapshotStorage reportSnapshotStorage() {
            return new ReportSnapshotStorage() {
                @Override
                public PresignedUpload createUploadUrl(String objectKey, Duration expiration) {
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
                public String createViewUrl(String objectKey, Duration expiration) {
                    return "https://view.example.com/" + objectKey;
                }
            };
        }
    }
}
