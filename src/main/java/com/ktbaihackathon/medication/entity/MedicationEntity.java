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

    @Column(name = "is_taken", nullable = false)
    private boolean isTaken = false; // 기본값 false 설정

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 생성자 (접근 제한: PROTECTED)
    private MedicationEntity(
            User user,
            String frontImageUrl,
            String backImageUrl,
            String drugName
    ) {
        this.user = user;
        this.frontImageUrl = frontImageUrl;
        this.backImageUrl = backImageUrl;
        this.drugName = drugName;
        this.isTaken = false;
        this.createdAt = LocalDateTime.now();
    }

    // 정적 팩토리 메서드 (알약 이미지 업로드 및 판별 시점에 사용)
    public static MedicationEntity createRecognition(
            User user,
            String frontImageUrl,
            String backImageUrl,
            String drugName
    ) {
        return new MedicationEntity(user, frontImageUrl, backImageUrl, drugName);
    }

    // 비즈니스 로직: 팝업창에서 "YES(섭취함)"를 눌렀을 때 상태 변경 메서드
    public void markAsTaken() {
        this.isTaken = true;
        this.takenAt = LocalDateTime.now(); // 복용 시작 시간 기록
    }
}