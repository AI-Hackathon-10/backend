package com.ktbaihackathon.medication.entity;

import com.ktbaihackathon.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_recognition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_recognition_id") // 🚨 기획서 매핑 수정
    private Long medicationRecognitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "front_image_object_key", length = 500) // 🚨 URL 대신 Object Key 명세 적용
    private String frontImageObjectKey;

    @Column(name = "back_image_object_key", length = 500)  // 🚨 URL 대신 Object Key 명세 적용
    private String backImageObjectKey;

    @Column(name = "analysis_status", length = 20, nullable = false) // 🚨 추가된 상태값 필드
    private String analysisStatus;

    @Column(name = "drug_name", length = 100)
    private String drugName;

    @Column(name = "request_id", length = 100, unique = true)
    private String requestId;

    @Column(name = "is_taken", nullable = false)
    private boolean isTaken = false;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private MedicationEntity(
            User user,
            String requestId,
            String frontImageObjectKey,
            String backImageObjectKey,
            String drugName
    ) {
        this.user = user;
        this.requestId = requestId;
        this.frontImageObjectKey = frontImageObjectKey;
        this.backImageObjectKey = backImageObjectKey;
        this.drugName = drugName;
        this.analysisStatus = "PENDING"; // 🚨 생성 시 기본 상태는 PENDING
        this.isTaken = false;
        this.createdAt = LocalDateTime.now();
    }

    public static MedicationEntity createRecognition(
            User user,
            String requestId,
            String frontImageObjectKey,
            String backImageObjectKey,
            String drugName
    ) {
        return new MedicationEntity(user, requestId, frontImageObjectKey, backImageObjectKey, drugName);
    }

    // AI 결과 업데이트 및 상태 완료 처리
    public void updateIdentificationResult(String drugName, String status) {
        this.drugName = drugName;
        this.analysisStatus = status;
    }

    public void markAsTaken() {
        if (isTaken) {
            return;
        }

        this.isTaken = true;
        this.takenAt = LocalDateTime.now();
    }
}
