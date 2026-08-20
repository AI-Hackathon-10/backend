package com.ktbaihackathon.medication.dto;

public record FastApiIdentifyRequest(
        String frontImage,
        String backImage,
        String frontMimeType,
        String backMimeType
) {}