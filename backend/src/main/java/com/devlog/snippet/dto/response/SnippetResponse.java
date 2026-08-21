package com.devlog.snippet.dto.response;

import com.devlog.snippet.domain.Snippet;

import java.time.LocalDateTime;

public record SnippetResponse(
        Long id,
        String title,
        String description,
        String code,
        String language,
        String authorNickname,
        String authorUsername,
        Long viewCount,
        LocalDateTime createdAt
) {
    public static SnippetResponse of(Snippet s) {
        return new SnippetResponse(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getCode(),
                s.getLanguage(),
                s.getAuthor().getNickname(),
                s.getAuthor().getUsername(),
                s.getViewCount(),
                s.getCreatedAt()
        );
    }
}
