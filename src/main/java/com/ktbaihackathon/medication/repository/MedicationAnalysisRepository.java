package com.ktbaihackathon.medication.repository;

import com.ktbaihackathon.medication.entity.MedicationAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationAnalysisRepository
        extends JpaRepository<MedicationAnalysis, Long> {
}