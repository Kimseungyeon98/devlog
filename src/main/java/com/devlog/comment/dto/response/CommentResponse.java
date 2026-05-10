package com.devlog.comment.dto.response;

import com.devlog.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        String authorNickname,
        boolean isDeleted,
        LocalDateTime createdAt,
        List<CommentResponse> children  // 대댓글 목록
) {
    public static CommentResponse of(Comment comment, List<CommentResponse> children) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.isDeleted() ? null : comment.getAuthor().getNickname(),
                comment.isDeleted(),
                comment.getCreatedAt(),
                children
        );
    }
}