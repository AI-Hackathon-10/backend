package com.ktbaihackathon.user.dto;

import com.ktbaihackathon.user.entity.User;

public record SignUpResponse(
        Long userId,
        String loginId,
        String name
) {

    public static SignUpResponse from(User user) {
        return new SignUpResponse(user.getUserId(), user.getLoginId(), user.getName());
    }
}
