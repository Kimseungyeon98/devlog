package com.devlog.stat.dto.response;

import java.time.LocalDateTime;

public record PostStatResponse(
        Long postId,
        String title,
        Long viewCount,
        LocalDateTime createdAt
) {}
