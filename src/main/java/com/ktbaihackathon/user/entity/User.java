package com.ktbaihackathon.user.entity;

import com.ktbaihackathon.user.enums.Gender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    private User(
            String name,
            String loginId,
            String password,
            Gender gender,
            LocalDate birthDate
    ) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public static User createUser(
            String name,
            String loginId,
            String password,
            Gender gender,
            LocalDate birthDate
    ) {
        return new User(
                name,
                loginId,
                password,
                gender,
                birthDate
        );
    }
}