package com.ktbaihackathon.medication.dto;

public record PillUploadUrlsResponse(
        Long medicationId,
        String requestId,
        String frontUploadUrl,
        String backUploadUrl
) {}
