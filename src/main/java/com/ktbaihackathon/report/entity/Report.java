package com.ktbaihackathon.report.entity;

import com.ktbaihackathon.common.entity.BaseTimeEntity;
import com.ktbaihackathon.medication.entity.MedicationEntity;
import com.ktbaihackathon.symptom.entity.SymptomRecord;
import com.ktbaihackathon.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symptom_record_id", nullable = false)
    private SymptomRecord symptomRecord;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "drug_recognition_id",
            nullable = false,
            unique = true
    )
    private MedicationEntity medication;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /*
     * Presigned URL이 아니라 S3 Object Key를 저장합니다.
     * Presigned URL은 조회 시 새로 발급합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_status", nullable = false, length = 20)
    private ReportSnapshotStatus snapshotStatus;

    @Column(name = "snapshot_object_key", length = 1024)
    private String snapshotObjectKey;

    private Report(
            User user,
            SymptomRecord symptomRecord,
            MedicationEntity medication,
            String summary
    ) {
        this.user = user;
        this.symptomRecord = symptomRecord;
        this.medication = medication;
        this.summary = summary;
        this.snapshotStatus = ReportSnapshotStatus.PENDING;
    }

    public static Report create(
            User user,
            SymptomRecord symptomRecord,
            MedicationEntity medication,
            String summary
    ) {
        return new Report(user, symptomRecord, medication, summary);
    }

    public void prepareSnapshotUpload(
            String snapshotObjectKey
    ) {
        this.snapshotObjectKey = snapshotObjectKey;
        this.snapshotStatus = ReportSnapshotStatus.PENDING;
    }

    public void completeSnapshotUpload() {
        if (snapshotObjectKey == null) {
            throw new IllegalStateException(
                    "스냅샷 Object Key가 존재하지 않습니다."
            );
        }

        this.snapshotStatus = ReportSnapshotStatus.UPLOADED;
    }
}
