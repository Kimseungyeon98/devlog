package com.devlog.snippet.dto.response;

import com.devlog.snippet.domain.Snippet;

import java.time.LocalDateTime;

public record SnippetSummaryResponse(
        Long id,
        String title,
        String description,
        String language,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt
) {
    public static SnippetSummaryResponse of(Snippet s) {
        return new SnippetSummaryResponse(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getLanguage(),
                s.getAuthor().getNickname(),
                s.getViewCount(),
                s.getCreatedAt()
        );
    }
}
