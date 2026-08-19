package com.ktbaihackathon.symptom.repository;

import com.ktbaihackathon.symptom.entity.SymptomRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SymptomRecordRepository extends JpaRepository<SymptomRecord, Long> {
}
