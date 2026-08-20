package com.ktbaihackathon.medication.dto;

public record MedicationIdentifyResponse(
        boolean ok,
        String confidence,
        double score,
        String itemSeq,
        String itemName,
        String imageUrl,
        Features features,
        Official official,
        String document
) {

    public record Features(
            String frontImprint,
            String backImprint,
            String shape,
            String color,
            boolean scoreLine
    ) {
    }

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
    ) {
    }
}