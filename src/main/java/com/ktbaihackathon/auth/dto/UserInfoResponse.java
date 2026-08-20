package com.ktbaihackathon.auth.dto;

import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.enums.Gender;

import java.time.LocalDate;

public record UserInfoResponse(
        Long userId,
        String loginId,
        String name,
        Gender gender,
        LocalDate birthDate
) {

    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getName(),
                user.getGender(),
                user.getBirthDate()
        );
    }
}
