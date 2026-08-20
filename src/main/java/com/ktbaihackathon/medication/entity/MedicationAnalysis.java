package com.ktbaihackathon.medication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MedicationAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medication_analysis_id")
    private Long id;

    // MedicationRecognition 엔티티 병합 후 연관관계로 변경
    @Column(name = "medication_recognition_id", nullable = false)
    private Long medicationRecognitionId;

    // SymptomRecord 구현 상태 확인 후 연관관계로 변경 가능
    @Column(name = "symptom_record_id")
    private Long symptomRecordId;

    @Column(name = "item_seq", length = 50)
    private String itemSeq;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "identification_confidence", length = 20)
    private String identificationConfidence;

    @Column(name = "identification_score")
    private Double identificationScore;

    @Column(name = "recommendation_status", length = 30)
    private String recommendationStatus;

    @Column(name = "recommendation_score")
    private Double recommendationScore;

    @Column(name = "recommendation_confidence", length = 20)
    private String recommendationConfidence;

    @Lob
    @Column(name = "recommendation_reason")
    private String recommendationReason;

    @Lob
    @Column(name = "recommendation_caution")
    private String recommendationCaution;

    @Column(name = "front_imprint", length = 100)
    private String frontImprint;

    @Column(name = "back_imprint", length = 100)
    private String backImprint;

    @Column(name = "shape", length = 50)
    private String shape;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "score_line")
    private Boolean scoreLine;

    @Lob
    @Column(name = "efficacy")
    private String efficacy;

    @Lob
    @Column(name = "use_method")
    private String useMethod;

    @Lob
    @Column(name = "warning")
    private String warning;

    @Lob
    @Column(name = "caution")
    private String caution;

    @Lob
    @Column(name = "interaction")
    private String interaction;

    @Lob
    @Column(name = "side_effect")
    private String sideEffect;

    @Lob
    @Column(name = "storage")
    private String storage;

    @Lob
    @Column(name = "document")
    private String document;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}