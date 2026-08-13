package com.devlog.post.controller;

import com.devlog.post.dto.request.CreatePostRequest;
import com.devlog.post.dto.request.PostSearchRequest;
import com.devlog.post.dto.request.UpdatePostRequest;
import com.devlog.post.dto.response.PostResponse;
import com.devlog.post.dto.response.PostSummaryResponse;
import com.devlog.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.create(userId, request));
    }

    // 게시글 단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPost(postId));
    }

    // 게시글 목록 검색
    @GetMapping
    public ResponseEntity<Page<PostSummaryResponse>> search(
            @ModelAttribute PostSearchRequest request,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.search(request, pageable));
    }

    // 내 게시글 목록
    @GetMapping("/me")
    public ResponseEntity<Page<PostSummaryResponse>> getMyPosts(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getMyPosts(userId, pageable));
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @RequestBody @Valid UpdatePostRequest request) {
        postService.update(userId, postId, request);
        return ResponseEntity.noContent().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        postService.delete(userId, postId);
        return ResponseEntity.noContent().build();
    }
}