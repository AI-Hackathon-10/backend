package com.ktbaihackathon.symptom.entity;

import com.ktbaihackathon.common.entity.BaseTimeEntity;
import com.ktbaihackathon.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "symptom_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SymptomRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptom_record_id")
    private Long symptomRecordId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @OneToMany(
            mappedBy = "symptomRecord",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("symptomMapId ASC")
    private List<SymptomMap> symptomMaps = new ArrayList<>();

    private SymptomRecord(
            User user,
            Instant startedAt,
            String memo
    ) {
        this.user = user;
        this.startedAt = startedAt;
        this.memo = memo;
    }

    public static SymptomRecord create(
            User user,
            Instant startedAt,
            String memo
    ) {
        return new SymptomRecord(user, startedAt, memo);
    }

    public void addSymptom(SymptomType symptomType) {
        if (symptomMaps.size() >= 10) {
            throw new IllegalArgumentException("증상은 최대 10개까지 선택할 수 있습니다.");
        }

        boolean alreadyExists = symptomMaps.stream()
                .anyMatch(symptomMap ->
                        symptomMap.getSymptomType() == symptomType
                );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 선택한 증상입니다.");
        }

        symptomMaps.add(SymptomMap.create(this, symptomType));
    }
}
