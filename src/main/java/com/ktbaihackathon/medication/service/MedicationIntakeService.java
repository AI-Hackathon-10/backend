package com.ktbaihackathon.medication.service;

import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.medication.dto.MedicationIntakeResponse;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.medication.repository.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicationIntakeService {

    private final MedicationRepository medicationRepository;

    @Transactional
    public MedicationIntakeResponse markAsTaken(Long userId, Long medicationId) {
        MedicationEntity medication = medicationRepository.findById(medicationId)
                .filter(item -> item.getUser() != null
                        && userId.equals(item.getUser().getUserId()))
                .orElseThrow(() -> new CustomException(ResultCode.MEDICATION_NOT_FOUND));

        if (!medication.isTaken()) {
            medication.markAsTaken();
        }

        return MedicationIntakeResponse.from(medication);
    }
}
