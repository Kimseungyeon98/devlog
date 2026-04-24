package com.devlog.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(min = 2, max = 30, message = "닉네임은 2~30자 사이여야 합니다.")
        String nickname,

        @Size(max = 500, message = "소개는 500자 이하여야 합니다.")
        String bio,

        String profileImageUrl
) {}