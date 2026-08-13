package com.devlog.comment.event;

public record CommentEvent(
        Long actorId,           // 됓글 단 사람
        Long postAuthorId,      // 게시글 작성자 ID
        Long parentAuthorId,    // 부모 됓글 작성자 ID (대됓글이면 non-null)
        Long postId,
        Long commentId
) {}
