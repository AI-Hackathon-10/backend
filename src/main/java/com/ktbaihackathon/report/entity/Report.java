package com.ktbaihackathon.report.entity;

import com.ktbaihackathon.common.entity.BaseTimeEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String userName;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "report_symptoms", joinColumns = @JoinColumn(name = "report_id"))
    @OrderColumn(name = "symptom_order")
    @Enumerated(EnumType.STRING)
    @Column(name = "symptom", nullable = false, length = 50)
    private List<SymptomType> symptoms = new ArrayList<>();

    @Column(nullable = false)
    private Instant symptomStartedAt;

    @Column(nullable = false, length = 40)
    private String timezoneId;

    @Column(nullable = false, length = 200)
    private String memo;

    @Column(nullable = false)
    private Long drugBolId;

    @Column(nullable = false, length = 100)
    private String drugName;

    @Column(nullable = false, length = 2048)
    private String drugImageUrl;

    @Column(nullable = false)
    private Instant takenAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportSnapshotStatus snapshotStatus;

    @Column(length = 1024)
    private String snapshotObjectKey;

    @Column(length = 255)
    private String snapshotFileName;

    private Long snapshotContentLength;

    private Report(
        Long userId,
        String userName,
        List<SymptomType> symptoms,
        Instant symptomStartedAt,
        String timezoneId,
        String memo,
        Long drugBolId,
        String drugName,
        String drugImageUrl,
        Instant takenAt
    ) {
        this.userId = userId;
        this.userName = userName;
        this.symptoms = new ArrayList<>(symptoms);
        this.symptomStartedAt = symptomStartedAt;
        this.timezoneId = timezoneId;
        this.memo = memo;
        this.drugBolId = drugBolId;
        this.drugName = drugName;
        this.drugImageUrl = drugImageUrl;
        this.takenAt = takenAt;
        this.snapshotStatus = ReportSnapshotStatus.PENDING;
    }

    public static Report create(
        Long userId,
        String userName,
        List<SymptomType> symptoms,
        Instant symptomStartedAt,
        String timezoneId,
        String memo,
        Long drugBolId,
        String drugName,
        String drugImageUrl,
        Instant takenAt
    ) {
        return new Report(
            userId,
            userName,
            symptoms,
            symptomStartedAt,
            timezoneId,
            memo,
            drugBolId,
            drugName,
            drugImageUrl,
            takenAt
        );
    }

    public void prepareSnapshotUpload(String objectKey, String fileName) {
        if (snapshotStatus == ReportSnapshotStatus.UPLOADED) {
            throw new IllegalStateException("이미 업로드된 문서 스냅샷은 변경할 수 없습니다.");
        }
        this.snapshotObjectKey = objectKey;
        this.snapshotFileName = fileName;
    }

    public void completeSnapshotUpload(long contentLength) {
        if (snapshotObjectKey == null) {
            throw new IllegalStateException("문서 스냅샷 업로드 URL이 발급되지 않았습니다.");
        }
        this.snapshotContentLength = contentLength;
        this.snapshotStatus = ReportSnapshotStatus.UPLOADED;
    }
}
