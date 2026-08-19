package com.ktbaihackathon.auth.service;

import com.ktbaihackathon.auth.dto.LoginRequest;
import com.ktbaihackathon.auth.dto.LoginResponse;
import com.ktbaihackathon.auth.entity.RefreshToken;
import com.ktbaihackathon.auth.repository.RefreshTokenRepository;
import com.ktbaihackathon.common.exception.CustomException;
import com.ktbaihackathon.common.jwt.JwtProvider;
import com.ktbaihackathon.common.response.ResultCode;
import com.ktbaihackathon.common.security.PasswordEncoder;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new CustomException(ResultCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ResultCode.LOGIN_FAILED);
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId(), user.getName());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        LocalDateTime expiresAt = jwtProvider.getExpiration(refreshToken);

        refreshTokenRepository.findByUserId(user.getUserId())
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiresAt),
                        () -> refreshTokenRepository.save(RefreshToken.create(user.getUserId(), refreshToken, expiresAt))
                );

        return LoginResponse.of(accessToken, refreshToken);
    }
}
