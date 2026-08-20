package com.ktbaihackathon.voice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FastApiSymptomResponse(
        @JsonProperty("parsedSymptoms") List<String> parsedSymptoms,
        @JsonProperty("symptomStartedAt") String symptomStartedAt,
        @JsonProperty("onsetConfidence") String onsetConfidence,
        List<Candidate> candidates,
        String notice,
        String disclaimer
) {
    public record Candidate(
            @JsonProperty("itemSeq") String itemSeq,
            @JsonProperty("itemName") String itemName,
            @JsonProperty("imageUrl") String imageUrl,
            Official official,
            Recommendation recommendation,
            String source
    ) {}

    public record Official(
            @JsonProperty("itemSeq") String itemSeq,
            @JsonProperty("itemName") String itemName,
            String efficacy,
            @JsonProperty("useMethod") String useMethod,
            String warning,
            String caution,
            String interaction,
            @JsonProperty("sideEffect") String sideEffect,
            String storage,
            @JsonProperty("imageUrl") String imageUrl
    ) {}

    public record Recommendation(
            String status,
            double score,
            String confidence,
            String reason,
            String caution
    ) {}
}
