package com.ktbaihackathon.report.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SymptomTypeTest {

    @Test
    void containsAllTwentySelectableSymptoms() {
        assertThat(SymptomType.values()).hasSize(20);
        assertThat(SymptomType.values())
            .extracting(SymptomType::getDisplayName)
            .containsExactly(
                "두통", "발열", "기침", "인후통", "콧물", "코막힘", "복통", "소화불량", "설사", "변비",
                "속쓰림", "구토/메스꺼움", "근육통", "생리통", "치통", "알레르기", "피부 가려움", "몸살",
                "어지러움", "오한"
            );
    }

    @Test
    void acceptsKoreanDisplayNameFromJson() {
        assertThat(SymptomType.fromJson("두통")).isEqualTo(SymptomType.HEADACHE);
        assertThat(SymptomType.fromJson("오한")).isEqualTo(SymptomType.CHILLS);
    }
}
