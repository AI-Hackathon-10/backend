package com.ktbaihackathon.auth.controller;

import com.ktbaihackathon.auth.dto.LoginRequest;
import com.ktbaihackathon.auth.dto.LoginResponse;
import com.ktbaihackathon.auth.service.AuthService;
import com.ktbaihackathon.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ktbaihackathon.common.response.ResultCode.LOGIN_SUCCESS;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ApiResponse.success(
                LOGIN_SUCCESS.name(),
                LOGIN_SUCCESS.getMessage(),
                response
        );
    }
}
