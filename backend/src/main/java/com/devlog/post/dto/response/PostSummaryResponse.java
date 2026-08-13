package com.devlog.post.dto.response;

import com.devlog.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
        Long id,
        String title,
        String status,
        Long viewCount,
        String authorNickname,
        List<String> tags,
        LocalDateTime createdAt
) {
    public static PostSummaryResponse of(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getStatus().name(),
                post.getViewCount(),
                post.getAuthor().getNickname(),
                post.getPostTags().stream()
                        .map(pt -> pt.getTag().getName())
                        .toList(),
                post.getCreatedAt()
        );
    }
}