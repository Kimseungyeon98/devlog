package com.devlog.series.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSeriesRequest(
        @NotBlank(message = "시리즈 제목을 입력해주세요.")
        @Size(max = 100)
        String title,

        @Size(max = 1000)
        String description
) {}
