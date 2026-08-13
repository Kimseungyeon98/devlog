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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LikeService 단위 테스트")
class LikeServiceTest {

    @Mock LikeRepository likeRepository;
    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @InjectMocks LikeService likeService;

    User liker;
    User postAuthor;
    Post post;
    Comment comment;

    @BeforeEach
    void setUp() {
        liker = User.createLocalUser("liker", "liker@test.com", "hash", "좋아요러");
        ReflectionTestUtils.setField(liker, "id", 1L);

        postAuthor = User.createLocalUser("author", "author@test.com", "hash", "글쓴이");
        ReflectionTestUtils.setField(postAuthor, "id", 2L);

        post = Post.create(postAuthor, "제목", "내용");
        ReflectionTestUtils.setField(post, "id", 10L);

        comment = Comment.create(post, postAuthor, "댓글");
        ReflectionTestUtils.setField(comment, "id", 20L);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Nested
    @DisplayName("togglePostLike")
    class TogglePostLike {
        @Test
        @DisplayName("좋아요가 없으면 추가하고 true를 반환하며 이벤트를 발행한다")
        void like() {
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(userRepository.findById(1L)).willReturn(Optional.of(liker));
            given(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 10L, LikeTarget.POST))
                    .willReturn(Optional.empty());

            boolean result = likeService.togglePostLike(1L, 10L);

            assertThat(result).isTrue();
            then(likeRepository).should().save(any(Like.class));

            ArgumentCaptor<LikeEvent> captor = ArgumentCaptor.forClass(LikeEvent.class);
            then(eventPublisher).should().publishEvent(captor.capture());
            assertThat(captor.getValue().actorId()).isEqualTo(1L);
            assertThat(captor.getValue().targetOwnerId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("좋아요가 이미 있으면 취소하고 false를 반환하며 이벤트를 발행하지 않는다")
        void unlike() {
            Like existingLike = Like.of(liker, 10L, LikeTarget.POST);
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(userRepository.findById(1L)).willReturn(Optional.of(liker));
            given(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 10L, LikeTarget.POST))
                    .willReturn(Optional.of(existingLike));

            boolean result = likeService.togglePostLike(1L, 10L);

            assertThat(result).isFalse();
            then(likeRepository).should().delete(existingLike);
            then(eventPublisher).should(never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("getLikeCount")
    class GetLikeCount {
        @Test
        @DisplayName("Redis 캐시가 있으면 DB를 조회하지 않는다")
        void cacheHit() {
            given(valueOperations.get(anyString())).willReturn("42");

            long count = likeService.getLikeCount(10L, LikeTarget.POST);

            assertThat(count).isEqualTo(42L);
            then(likeRepository).should(never()).countByTargetIdAndTargetType(any(), any());
        }

        @Test
        @DisplayName("Redis 캐시가 없으면 DB를 조회하고 캐싱한다")
        void cacheMiss() {
            given(valueOperations.get(anyString())).willReturn(null);
            given(likeRepository.countByTargetIdAndTargetType(10L, LikeTarget.POST)).willReturn(5L);

            long count = likeService.getLikeCount(10L, LikeTarget.POST);

            assertThat(count).isEqualTo(5L);
            then(valueOperations).should().set(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("isLiked")
    class IsLiked {
        @Test
        @DisplayName("좋아요 여부를 repository에 위임한다")
        void delegates() {
            given(likeRepository.existsByUserIdAndTargetIdAndTargetType(1L, 10L, LikeTarget.POST))
                    .willReturn(true);

            assertThat(likeService.isLiked(1L, 10L, LikeTarget.POST)).isTrue();
        }
    }
}
