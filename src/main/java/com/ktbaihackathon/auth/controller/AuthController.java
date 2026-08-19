package com.ktbaihackathon.auth.controller;

import com.ktbaihackathon.auth.dto.LoginRequest;
import com.ktbaihackathon.auth.dto.LoginResponse;
import com.ktbaihackathon.auth.dto.RefreshRequest;
import com.ktbaihackathon.auth.dto.UserInfoResponse;
import com.ktbaihackathon.auth.service.AuthService;
import com.ktbaihackathon.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.ktbaihackathon.common.response.ResultCode.AUTH_CHECK_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.LOGIN_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.LOGOUT_SUCCESS;
import static com.ktbaihackathon.common.response.ResultCode.TOKEN_REISSUE_SUCCESS;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping
    public ApiResponse<UserInfoResponse> me(@RequestAttribute("userId") Long userId) {
        UserInfoResponse response = authService.getMe(userId);

        return ApiResponse.success(
                AUTH_CHECK_SUCCESS.name(),
                AUTH_CHECK_SUCCESS.getMessage(),
                response
        );
    }

    @PostMapping
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ApiResponse.success(
                LOGIN_SUCCESS.name(),
                LOGIN_SUCCESS.getMessage(),
                response
        );
    }

    @PutMapping
    public ApiResponse<LoginResponse> reissue(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.reissue(request);

        return ApiResponse.success(
                TOKEN_REISSUE_SUCCESS.name(),
                TOKEN_REISSUE_SUCCESS.getMessage(),
                response
        );
    }

    @DeleteMapping
    public ApiResponse<Void> logout(@RequestAttribute("userId") Long userId) {
        authService.logout(userId);

        return ApiResponse.success(
                LOGOUT_SUCCESS.name(),
                LOGOUT_SUCCESS.getMessage()
        );
    }
}
