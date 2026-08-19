package com.ktbaihackathon.auth.service;

import com.ktbaihackathon.auth.dto.LoginRequest;
import com.ktbaihackathon.auth.dto.LoginResponse;
import com.ktbaihackathon.auth.dto.RefreshRequest;
import com.ktbaihackathon.auth.dto.UserInfoResponse;
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

    @Transactional
    public LoginResponse reissue(RefreshRequest request) {
        String refreshToken = request.refreshToken();

        Long userId;
        try {
            if (!jwtProvider.isRefreshToken(refreshToken)) {
                throw new CustomException(ResultCode.INVALID_TOKEN);
            }
            userId = jwtProvider.getUserId(refreshToken);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ResultCode.INVALID_TOKEN);
        }

        RefreshToken savedRefreshToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_TOKEN));

        if (!savedRefreshToken.getToken().equals(refreshToken)) {
            refreshTokenRepository.delete(savedRefreshToken);
            throw new CustomException(ResultCode.INVALID_TOKEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_TOKEN));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId(), user.getName());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId());
        LocalDateTime expiresAt = jwtProvider.getExpiration(newRefreshToken);

        savedRefreshToken.update(newRefreshToken, expiresAt);

        return LoginResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ResultCode.INVALID_TOKEN));

        return UserInfoResponse.from(user);
    }
}
