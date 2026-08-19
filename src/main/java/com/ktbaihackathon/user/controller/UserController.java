package com.ktbaihackathon.user.controller;

import com.ktbaihackathon.common.response.ApiResponse;
import com.ktbaihackathon.user.dto.SignUpRequest;
import com.ktbaihackathon.user.dto.SignUpResponse;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ktbaihackathon.common.response.ResultCode.USER_CREATE_SUCCESS;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        User user = userService.signUp(request);

        return ApiResponse.success(
                USER_CREATE_SUCCESS.name(),
                USER_CREATE_SUCCESS.getMessage(),
                SignUpResponse.from(user)
        );
    }
}
