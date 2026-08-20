package com.ktbaihackathon.medication.repository;

import com.ktbaihackathon.medication.entity.MedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<MedicationEntity, Long> {

    Optional<MedicationEntity> findByRequestIdAndUserUserId(String requestId, Long userId);

}
