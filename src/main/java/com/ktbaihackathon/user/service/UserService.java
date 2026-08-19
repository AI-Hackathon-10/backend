package com.ktbaihackathon.user.service;

import com.ktbaihackathon.common.security.PasswordEncoder;
import com.ktbaihackathon.user.dto.SignUpRequest;
import com.ktbaihackathon.user.entity.User;
import com.ktbaihackathon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signUp(SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createUser(
                request.name(),
                request.loginId(),
                encodedPassword,
                request.gender(),
                request.birthDate()
        );

        return userRepository.save(user);
    }
}
