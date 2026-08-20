package com.ktbaihackathon.medication.dto;

// 잭슨 의존성 없이 필드명 자체를 FastAPI API Docs 스펙 규격과 1:1로 일치시킴
public record FastApiIdentifyRequest(
        String front_image,
        String back_image,
        String front_mime_type,
        String back_mime_type
) {}