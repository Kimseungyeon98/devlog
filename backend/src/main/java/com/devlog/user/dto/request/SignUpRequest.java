package com.devlog.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank(message = "아이디를 입력해주세요.")
        @Size(min = 3, max = 30, message = "아이디는 3~30자 사이여야 합니다.")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "아이디는 소문자, 숫자, 언더스코어만 사용 가능합니다.")
        String username,

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자 사이여야 합니다.")
        String password,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2~30자 사이여야 합니다.")
        String nickname
) {}