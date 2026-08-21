package com.devlog.post.dto.request;

public record PostSearchRequest(
        String keyword,
        String tag
) {}