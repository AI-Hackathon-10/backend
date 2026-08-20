package com.ktbaihackathon.medication.dto;

public record FastApiIdentifyResponse(
        boolean visionFailed,
        Object matchResult,
        Object detail
) {}
