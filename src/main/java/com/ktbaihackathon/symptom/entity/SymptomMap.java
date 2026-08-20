package com.ktbaihackathon.symptom.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "symptom_map",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_symptom_record_type",
                        columnNames = {
                                "symptom_record_id",
                                "symptom_type"
                        }
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SymptomMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "symptom_map_id")
    private Long symptomMapId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symptom_record_id", nullable = false)
    private SymptomRecord symptomRecord;

    @Enumerated(EnumType.STRING)
    @Column(name = "symptom_type", nullable = false, length = 50)
    private SymptomType symptomType;

    private SymptomMap(
            SymptomRecord symptomRecord,
            SymptomType symptomType
    ) {
        this.symptomRecord = symptomRecord;
        this.symptomType = symptomType;
    }

    public static SymptomMap create(
            SymptomRecord symptomRecord,
            SymptomType symptomType
    ) {
        return new SymptomMap(symptomRecord, symptomType);
    }
}