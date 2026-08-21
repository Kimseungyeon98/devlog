package com.devlog.series.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddPostToSeriesRequest(
        @NotNull Long postId,
        int orderNum
) {}
