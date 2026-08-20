package com.ktbaihackathon.medication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ktbaihackathon.user.enums.Gender;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record FastApiIdentifyRequest(
        User user,
        List<String> symptoms,
        OffsetDateTime symptomStartedAt,
        List<Item> items
) {
    public record User(
            Long userId,
            String name,
            Gender gender,
            LocalDate birthDate
    ) {}

    public record Item(
            String id,
            @JsonProperty("front_image") String frontImage,
            @JsonProperty("back_image") String backImage
    ) {}
}
