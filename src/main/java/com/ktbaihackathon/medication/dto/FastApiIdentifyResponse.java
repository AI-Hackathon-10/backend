package com.ktbaihackathon.medication.dto;

import java.util.List;

public record FastApiIdentifyResponse(
        List<Result> result
) {
    public record Result(
            Boolean ok,
            String itemSeq,
            String itemName,
            String imageUrl,
            Identification identification,
            Recommendation recommendation,
            Features features,
            Official official,
            String document
    ) {}

    public record Identification(String confidence, Double score) {}

    public record Recommendation(
            String status, Double score, String confidence, String reason, String caution
    ) {}

    public record Features(
            String frontImprint, String backImprint, String shape, String color, Boolean scoreLine
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
