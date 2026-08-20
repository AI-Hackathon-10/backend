package com.ktbaihackathon.medication.repository;

import com.ktbaihackathon.medication.entity.MedicationIntake;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationIntakeRepository
        extends JpaRepository<MedicationIntake, Long> {
}