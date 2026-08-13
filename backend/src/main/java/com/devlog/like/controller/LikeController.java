package com.devlog.like.controller;

import com.devlog.like.domain.LikeTarget;
import com.devlog.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikeController {

    private final LikeService likeService;

    // 게시글 좋아요 토글
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<Boolean> togglePostLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        return ResponseEntity.ok(likeService.togglePostLike(userId, postId));
    }

    // 됓글 좋아요 토글
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<Boolean> toggleCommentLike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        return ResponseEntity.ok(likeService.toggleCommentLike(userId, commentId));
    }

    // 게시글 좋아요 수 조회
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<Long> getPostLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId, LikeTarget.POST));
    }

    // 됓글 좋아요 수 조회
    @GetMapping("/comments/{commentId}/likes")
    public ResponseEntity<Long> getCommentLikeCount(@PathVariable Long commentId) {
        return ResponseEntity.ok(likeService.getLikeCount(commentId, LikeTarget.COMMENT));
    }
}