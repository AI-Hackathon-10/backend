package com.ktbaihackathon.medication.dto;

public record PillUploadUrlsResponse(
        String requestId,
        String frontUploadUrl,
        String backUploadUrl
) {}
