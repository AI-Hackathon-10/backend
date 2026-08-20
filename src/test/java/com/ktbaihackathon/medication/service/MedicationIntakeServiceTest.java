package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.medication.dto.MedicationIntakeResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import com.ktbaihackathon.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MedicationIntakeServiceTest {

    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final MedicationIntakeService medicationIntakeService =
            new MedicationIntakeService(medicationRepository);

    @Test
    void confirmsMedicationIntakeForOwner() {
        User user = mock(User.class);
        MedicationEntity medication = mock(MedicationEntity.class);
        LocalDateTime takenAt = LocalDateTime.of(2026, 8, 20, 2, 30);

        when(medicationRepository.findById(7L)).thenReturn(Optional.of(medication));
        when(medication.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(medication.isTaken()).thenReturn(false);
        when(medication.getMedicationRecognitionId()).thenReturn(7L);
        when(medication.getDrugName()).thenReturn("타이레놀");
        when(medication.getTakenAt()).thenReturn(takenAt);

        MedicationIntakeResponse response = medicationIntakeService.markAsTaken(1L, 7L);

        verify(medication).markAsTaken();
        assertThat(response.medicationId()).isEqualTo(7L);
        assertThat(response.drugName()).isEqualTo("타이레놀");
        assertThat(response.takenAt()).isEqualTo(takenAt);
    }

    @Test
    void preservesOriginalIntakeTimeWhenAlreadyConfirmed() {
        User user = mock(User.class);
        MedicationEntity medication = mock(MedicationEntity.class);
        LocalDateTime originalTakenAt = LocalDateTime.of(2026, 8, 20, 2, 30);

        when(medicationRepository.findById(7L)).thenReturn(Optional.of(medication));
        when(medication.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(1L);
        when(medication.isTaken()).thenReturn(true);
        when(medication.getMedicationRecognitionId()).thenReturn(7L);
        when(medication.getTakenAt()).thenReturn(originalTakenAt);

        MedicationIntakeResponse response = medicationIntakeService.markAsTaken(1L, 7L);

        verify(medication, never()).markAsTaken();
        assertThat(response.takenAt()).isEqualTo(originalTakenAt);
    }

    @Test
    void rejectsMedicationOwnedByAnotherUser() {
        User user = mock(User.class);
        MedicationEntity medication = mock(MedicationEntity.class);

        when(medicationRepository.findById(7L)).thenReturn(Optional.of(medication));
        when(medication.getUser()).thenReturn(user);
        when(user.getUserId()).thenReturn(2L);

        assertThatThrownBy(() -> medicationIntakeService.markAsTaken(1L, 7L))
                .isInstanceOf(CustomException.class);

        verify(medication, never()).markAsTaken();
    }
}
