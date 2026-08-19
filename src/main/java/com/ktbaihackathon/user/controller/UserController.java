package com.ktbaihackathon.user.controller;

import com.ktbaihackathon.user.dto.SignUpRequest;
import com.ktbaihackathon.user.dto.SignUpResponse;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        User user = userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SignUpResponse.from(user));
    }
}
