package com.devlog.stat.dto.response;

import java.util.List;

public record StatsResponse(
        int totalPosts,
        long totalViews,
        List<PostStatResponse> posts
) {}
