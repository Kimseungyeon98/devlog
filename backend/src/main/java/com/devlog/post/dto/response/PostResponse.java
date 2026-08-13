package com.devlog.post.dto.response;

import com.devlog.post.domain.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        String status,
        Long viewCount,
        String authorUsername,
        String authorNickname,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse of(Post post, Long viewCount) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getStatus().name(),
                viewCount,
                post.getAuthor().getUsername(),
                post.getAuthor().getNickname(),
                post.getPostTags().stream()
                        .map(pt -> pt.getTag().getName())
                        .toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
