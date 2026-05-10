package com.devlog.post.service;

import com.devlog.post.domain.Post;
import com.devlog.post.domain.Tag;
import com.devlog.post.dto.request.CreatePostRequest;
import com.devlog.post.dto.request.PostSearchRequest;
import com.devlog.post.dto.request.UpdatePostRequest;
import com.devlog.post.dto.response.PostResponse;
import com.devlog.post.dto.response.PostSummaryResponse;
import com.devlog.post.repository.PostRepository;
import com.devlog.post.repository.TagRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VIEW_COUNT_KEY = "views:";

    private final EntityManager entityManager;

    // 게시글 작성
    @Transactional
    public Long create(Long userId, CreatePostRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Post post = Post.create(author, request.title(), request.content());

        if (request.publish()) {
            post.publish();
        }

        // 태그 처리
        if (request.tags() != null && !request.tags().isEmpty()) {
            applyTags(post, request.tags());
        }

        return postRepository.save(post).getId();
    }

    // 게시글 단건 조회
    @Transactional
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        // Redis 조회수 증가
        redisTemplate.opsForValue().increment(VIEW_COUNT_KEY + postId);

        // Redis 조회수 반영 (없으면 DB 값 사용)
        Long viewCount = getViewCount(postId, post.getViewCount());

        return PostResponse.of(post, viewCount);
    }

    // 게시글 목록 검색
    public Page<PostSummaryResponse> search(PostSearchRequest request, Pageable pageable) {
        return postRepository.search(request, pageable)
                .map(PostSummaryResponse::of);
    }

    // 내 게시글 목록
    public Page<PostSummaryResponse> getMyPosts(Long userId, Pageable pageable) {
        List<Post> posts = postRepository.findByAuthorIdWithAuthor(userId);
        return new PageImpl<>(posts, pageable, posts.size())
                .map(PostSummaryResponse::of);
    }

    // 게시글 수정
    @Transactional
    public void update(Long userId, Long postId, UpdatePostRequest request) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.isAuthor(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(request.title(), request.content());

        if (request.publish()) {
            post.publish();
        }

        // 태그 초기화 후 flush로 즉시 DB 반영
        post.clearTags();
        entityManager.flush();

        if (request.tags() != null && !request.tags().isEmpty()) {
            applyTags(post, request.tags());
        }
    }

    // 게시글 삭제
    @Transactional
    public void delete(Long userId, Long postId) {
        Post post = postRepository.findByIdWithAuthor(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.isAuthor(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        // Redis 조회수 키 삭제
        redisTemplate.delete(VIEW_COUNT_KEY + postId);

        postRepository.delete(post);
    }

    // 태그 처리 — 없으면 생성, 있으면 재사용
    private void applyTags(Post post, List<String> tagNames) {
        tagNames.stream()
                .map(name -> name.toLowerCase().trim())
                .distinct()
                .forEach(name -> {
                    Tag tag = tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(Tag.of(name)));
                    post.addTag(tag);
                });
    }

    // Redis 조회수 조회
    private Long getViewCount(Long postId, Long dbViewCount) {
        String redisValue = redisTemplate.opsForValue().get(VIEW_COUNT_KEY + postId);
        if (redisValue != null) {
            return Long.parseLong(redisValue);
        }
        return dbViewCount;
    }
}