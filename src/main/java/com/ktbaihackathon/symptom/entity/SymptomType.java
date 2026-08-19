package com.ktbaihackathon.symptom.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum SymptomType {

    HEADACHE("두통"),
    FEVER("발열"),
    COUGH("기침"),
    SORE_THROAT("인후통"),
    RUNNY_NOSE("콧물"),
    NASAL_CONGESTION("코막힘"),
    ABDOMINAL_PAIN("복통"),
    INDIGESTION("소화불량"),
    DIARRHEA("설사"),
    CONSTIPATION("변비"),
    HEARTBURN("속쓰림"),
    NAUSEA_OR_VOMITING("구토/메스꺼움"),
    MUSCLE_PAIN("근육통"),
    MENSTRUAL_CRAMPS("생리통"),
    TOOTHACHE("치통"),
    ALLERGY("알레르기"),
    ITCHY_SKIN("피부 가려움"),
    BODY_ACHES("몸살"),
    DIZZINESS("어지러움"),
    CHILLS("오한");

    private final String displayName;

    SymptomType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SymptomType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type ->
                        type.name().equalsIgnoreCase(value)
                                || type.displayName.equals(value)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("지원하지 않는 증상입니다: " + value)
                );
    }
}