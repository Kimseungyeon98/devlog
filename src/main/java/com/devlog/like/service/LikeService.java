package com.devlog.like.service;

import com.devlog.comment.domain.Comment;
import com.devlog.comment.repository.CommentRepository;
import com.devlog.like.domain.Like;
import com.devlog.like.domain.LikeTarget;
import com.devlog.like.event.LikeEvent;
import com.devlog.like.repository.LikeRepository;
import com.devlog.post.domain.Post;
import com.devlog.post.repository.PostRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LIKE_COUNT_KEY = "like:count:";

    // 게시글 좋아요 토글
    @Transactional
    public boolean togglePostLike(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        return toggle(user, postId, LikeTarget.POST, post.getAuthor().getId());
    }

    // 댓글 좋아요 토글
    @Transactional
    public boolean toggleCommentLike(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        return toggle(user, commentId, LikeTarget.COMMENT, comment.getAuthor().getId());
    }

    // 좋아요 수 조회
    public long getLikeCount(Long targetId, LikeTarget targetType) {
        String key = LIKE_COUNT_KEY + targetType.name() + ":" + targetId;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return Long.parseLong(cached);
        }

        long count = likeRepository.countByTargetIdAndTargetType(targetId, targetType);
        redisTemplate.opsForValue().set(key, String.valueOf(count),
                Duration.ofMinutes(10)); // 10분 캐싱
        return count;
    }

    // 좋아요 여부 조회
    public boolean isLiked(Long userId, Long targetId, LikeTarget targetType) {
        return likeRepository.existsByUserIdAndTargetIdAndTargetType(
                userId, targetId, targetType);
    }

    private boolean toggle(User user, Long targetId,
                           LikeTarget targetType, Long targetOwnerId) {
        String key = LIKE_COUNT_KEY + targetType.name() + ":" + targetId;

        return likeRepository
                .findByUserIdAndTargetIdAndTargetType(user.getId(), targetId, targetType)
                .map(like -> {
                    likeRepository.delete(like);
                    redisTemplate.delete(key); // 캐시 삭제
                    return false;
                })
                .orElseGet(() -> {
                    likeRepository.save(Like.of(user, targetId, targetType));
                    redisTemplate.delete(key); // 캐시 삭제
                    eventPublisher.publishEvent(
                            new LikeEvent(user.getId(), targetOwnerId, targetId, targetType)
                    );
                    return true;
                });
    }
}