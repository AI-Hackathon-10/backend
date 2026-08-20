package com.ktbaihackathon.medication.entity;

import com.ktbaihackathon.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "drug_recognition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "drug_recognition_id")
    private Long drugRecognitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "front_image_url", length = 500)
    private String frontImageUrl;

    @Column(name = "back_image_url", length = 500)
    private String backImageUrl;

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
            String frontImageUrl,
            String backImageUrl,
            String drugName
    ) {
        this.user = user;
        this.requestId = requestId;
        this.frontImageUrl = frontImageUrl;
        this.backImageUrl = backImageUrl;
        this.drugName = drugName;
        this.isTaken = false;
        this.createdAt = LocalDateTime.now();
    }

    public static MedicationEntity createRecognition(
            User user,
            String requestId,
            String frontImageUrl,
            String backImageUrl,
            String drugName
    ) {
        return new MedicationEntity(user, requestId, frontImageUrl, backImageUrl, drugName);
    }

    public void updateIdentificationResult(String drugName) {
        this.drugName = drugName;
    }

    public void markAsTaken() {
        this.isTaken = true;
        this.takenAt = LocalDateTime.now();
    }
}