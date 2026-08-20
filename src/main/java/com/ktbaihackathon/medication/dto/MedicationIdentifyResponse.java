package com.ktbaihackathon.medication.dto;

/** FastAPI 응답을 프론트엔드 규격으로 정리한 단일 알약 판별 결과. */
public record MedicationIdentifyResponse(
        Boolean ok,
        String itemSeq,
        String itemName,
        String imageUrl,
        Identification identification,
        Recommendation recommendation,
        Features features,
        Official official,
        String document
) {
    public record Identification(
            String confidence,
            Double score
    ) {}

    public record Recommendation(
            String status,
            Double score,
            String confidence,
            String reason,
            String caution
    ) {}

    public record Features(
            String frontImprint,
            String backImprint,
            String shape,
            String color,
            Boolean scoreLine
    ) {}

    public record Official(
            String itemSeq,
            String itemName,
            String efficacy,
            String useMethod,
            String warning,
            String caution,
            String interaction,
            String sideEffect,
            String storage,
            String imageUrl
    ) {}
}
