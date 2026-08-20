package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.medication.dto.FastApiIdentifyRequest;
import com.ktbaihackathon.symptom.entity.SymptomType;
import com.ktbaihackathon.user.enums.Gender;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MedicationIdentifyServiceTest {

    @Test
    void createsFastApiRequestWithConfirmedSchema() {
        FastApiIdentifyRequest.User apiUser = new FastApiIdentifyRequest.User(
                1L,
                "홍길동",
                Gender.MALE,
                LocalDate.parse("1990-01-15")
        );
        FastApiIdentifyRequest.Item requestItem = new FastApiIdentifyRequest.Item(
                "1",
                "data:image/jpeg;base64,encoded-front-image",
                "data:image/jpeg;base64,encoded-back-image"
        );

        FastApiIdentifyRequest apiRequest = new FastApiIdentifyRequest(
                apiUser,
                List.of(SymptomType.HEADACHE.name(), SymptomType.FEVER.name()),
                OffsetDateTime.parse("2026-08-20T14:00:00+09:00"),
                List.of(requestItem)
        );

        assertThat(apiRequest.user().userId()).isEqualTo(1L);
        assertThat(apiRequest.user().name()).isEqualTo("홍길동");
        assertThat(apiRequest.user().gender()).isEqualTo(Gender.MALE);
        assertThat(apiRequest.user().birthDate()).isEqualTo(LocalDate.parse("1990-01-15"));
        assertThat(apiRequest.symptoms()).containsExactly("HEADACHE", "FEVER");
        assertThat(apiRequest.symptomStartedAt())
                .isEqualTo(OffsetDateTime.parse("2026-08-20T14:00:00+09:00"));
        assertThat(apiRequest.items()).singleElement().satisfies(actualItem -> {
            assertThat(actualItem.id()).isEqualTo("1");
            assertThat(actualItem.frontImage()).isEqualTo("data:image/jpeg;base64,encoded-front-image");
            assertThat(actualItem.backImage()).isEqualTo("data:image/jpeg;base64,encoded-back-image");
        });
    }

    @Test
    void serializesFastApiItemImageAsFrontImage() {
        ObjectMapper objectMapper = new ObjectMapper();
        FastApiIdentifyRequest.Item item = new FastApiIdentifyRequest.Item(
                "1", "front-image-data", "back-image-data"
        );

        String json = objectMapper.writeValueAsString(item);

        assertThat(json).isEqualTo(
                "{\"id\":\"1\",\"front_image\":\"front-image-data\",\"back_image\":\"back-image-data\"}"
        );
    }
}
