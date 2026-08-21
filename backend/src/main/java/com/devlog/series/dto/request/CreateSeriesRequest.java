package com.devlog.series.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSeriesRequest(
        @NotBlank(message = "시리즈 제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
        String description
) {}
