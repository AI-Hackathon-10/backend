package com.ktbaihackathon.voice.dto;

import com.ktbaihackathon.user.enums.Gender;

import java.time.LocalDate;

public record FastApiSymptomRequest(User user, Voice voice) {

    public record User(Long userId, String name, Gender gender, LocalDate birthDate) {}

    public record Voice(String symptomText, String onsetText) {}
}
