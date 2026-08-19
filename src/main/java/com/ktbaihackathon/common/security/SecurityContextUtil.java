package com.ktbaihackathon.common.security;

import com.ktbaihackathon.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;

import static com.ktbaihackathon.common.response.ResultCode.INVALID_TOKEN;

public class SecurityContextUtil {
    public static Long extractUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if( userId== null){
            throw new CustomException(INVALID_TOKEN);
        }
        return userId;
    }
}
