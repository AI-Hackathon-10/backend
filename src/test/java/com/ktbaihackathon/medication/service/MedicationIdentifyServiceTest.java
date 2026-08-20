package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.medication.dto.FastApiIdentifyRequest;
import com.ktbaihackathon.medication.dto.IdentifyRequest;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.symptom.entity.SymptomType;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.enums.Gender;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedicationIdentifyServiceTest {

    @Test
    void createsFastApiRequestWithConfirmedSchema() {
        MedicationIdentifyService service = new MedicationIdentifyService(
                mock(S3ImageDownloadService.class),
                mock(MedicationRepository.class)
        );
        User user = mock(User.class);
        MedicationEntity medication = mock(MedicationEntity.class);
        when(user.getUserId()).thenReturn(1L);
        when(user.getName()).thenReturn("홍길동");
        when(user.getGender()).thenReturn(Gender.MALE);
        when(user.getBirthDate()).thenReturn(LocalDate.parse("1990-01-15"));
        when(medication.getUser()).thenReturn(user);
        when(medication.getMedicationRecognitionId()).thenReturn(1L);

        IdentifyRequest request = new IdentifyRequest(
                "request-1",
                List.of(SymptomType.HEADACHE, SymptomType.FEVER),
                OffsetDateTime.parse("2026-08-20T14:00:00+09:00")
        );

        FastApiIdentifyRequest apiRequest = service.createFastApiRequest(
                medication, request, "encoded-front-image", "encoded-back-image"
        );

        assertThat(apiRequest.user().userId()).isEqualTo(1L);
        assertThat(apiRequest.user().name()).isEqualTo("홍길동");
        assertThat(apiRequest.user().gender()).isEqualTo(Gender.MALE);
        assertThat(apiRequest.user().birthDate()).isEqualTo(LocalDate.parse("1990-01-15"));
        assertThat(apiRequest.symptoms()).containsExactly("HEADACHE", "FEVER");
        assertThat(apiRequest.symptomStartedAt())
                .isEqualTo(OffsetDateTime.parse("2026-08-20T14:00:00+09:00"));
        assertThat(apiRequest.items()).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo("1");
            assertThat(item.frontImage()).isEqualTo("data:image/jpeg;base64,encoded-front-image");
            assertThat(item.backImage()).isEqualTo("data:image/jpeg;base64,encoded-back-image");
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
