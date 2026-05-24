package com.devlog.comment.event;

public record CommentEvent(
        Long actorId,           // 댓글 단 사람
        Long postAuthorId,      // 게시글 작성자 ID
        Long parentAuthorId,    // 부모 댓글 작성자 ID (대댓글이면 non-null)
        Long postId,
        Long commentId
) {}
