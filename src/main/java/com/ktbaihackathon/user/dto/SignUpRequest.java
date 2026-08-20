package com.ktbaihackathon.user.dto;

import com.ktbaihackathon.user.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignUpRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
        String name,

        @NotBlank(message = "아이디는 필수입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자여야 합니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8~64자여야 합니다.")
        String password,

        @NotNull(message = "성별은 필수입니다.")
        Gender gender,

        @NotNull(message = "생년월일은 필수입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        LocalDate birthDate
) {
}
