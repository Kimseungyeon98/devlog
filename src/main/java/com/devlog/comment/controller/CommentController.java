package com.devlog.comment.controller;

import com.devlog.comment.dto.request.CreateCommentRequest;
import com.devlog.comment.dto.request.UpdateCommentRequest;
import com.devlog.comment.dto.response.CommentResponse;
import com.devlog.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @RequestBody @Valid CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(userId, postId, request));
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentRequest request) {
        commentService.update(userId, commentId, request);
        return ResponseEntity.noContent().build();
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId) {
        commentService.delete(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}