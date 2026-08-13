package com.devlog.post.service;

import com.devlog.post.domain.Post;
import com.devlog.post.domain.PostStatus;
import com.devlog.post.domain.Tag;
import com.devlog.post.dto.request.CreatePostRequest;
import com.devlog.post.dto.request.UpdatePostRequest;
import com.devlog.post.event.PostPublishedEvent;
import com.devlog.post.repository.PostRepository;
import com.devlog.post.repository.TagRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @Mock PostRepository postRepository;
    @Mock TagRepository tagRepository;
    @Mock UserRepository userRepository;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock EntityManager entityManager;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks PostService postService;

    User author;
    Post post;

    @BeforeEach
    void setUp() {
        author = User.createLocalUser("writer", "writer@test.com", "hashed", "작가");
        ReflectionTestUtils.setField(author, "id", 1L);

        post = Post.create(author, "제목", "내용");
        ReflectionTestUtils.setField(post, "id", 10L);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("draft으로 저장하면 이벤트를 발행하지 않는다")
        void createAsDraft() {
            given(userRepository.findById(1L)).willReturn(Optional.of(author));
            given(postRepository.save(any())).willReturn(post);
            CreatePostRequest req = new CreatePostRequest("제목", "내용", false, List.of());

            postService.create(1L, req);

            assertThat(post.getStatus()).isEqualTo(PostStatus.DRAFT);
            then(eventPublisher).should(never()).publishEvent(any(PostPublishedEvent.class));
        }

        @Test
        @DisplayName("publish=true로 저장하면 PUBLISHED 상태가 되고 이벤트를 발행한다")
        void createAsPublished() {
            given(userRepository.findById(1L)).willReturn(Optional.of(author));
            given(postRepository.save(any())).willReturn(post);
            CreatePostRequest req = new CreatePostRequest("제목", "내용", true, List.of());

            postService.create(1L, req);

            ArgumentCaptor<PostPublishedEvent> captor = ArgumentCaptor.forClass(PostPublishedEvent.class);
            then(eventPublisher).should().publishEvent(captor.capture());
            assertThat(captor.getValue().postTitle()).isEqualTo("제목");
        }

        @Test
        @DisplayName("태그와 함께 생성하면 태그가 적용된다")
        void createWithTags() {
            given(userRepository.findById(1L)).willReturn(Optional.of(author));
            given(postRepository.save(any())).willReturn(post);
            Tag tag = Tag.of("spring");
            given(tagRepository.findByName("spring")).willReturn(Optional.of(tag));
            CreatePostRequest req = new CreatePostRequest("제목", "내용", false, List.of("spring"));

            postService.create(1L, req);

            then(tagRepository).should().findByName("spring");
        }

        @Test
        @DisplayName("존재하지 않는 유저면 예외를 던진다")
        void userNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.create(99L,
                    new CreatePostRequest("t", "c", false, List.of())))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("draft → publish 전환 시 이벤트를 발행한다")
        void draftToPublished() {
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));
            UpdatePostRequest req = new UpdatePostRequest("수정제목", "수정내용", true, List.of());

            postService.update(1L, 10L, req);

            assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
            then(eventPublisher).should().publishEvent(any(PostPublishedEvent.class));
        }

        @Test
        @DisplayName("이미 published인 글을 수정해도 이벤트를 재발행하지 않는다")
        void alreadyPublishedNoEvent() {
            post.publish(); // 이미 PUBLISHED
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));
            UpdatePostRequest req = new UpdatePostRequest("수정제목", "수정내용", true, List.of());

            postService.update(1L, 10L, req);

            then(eventPublisher).should(never()).publishEvent(any(PostPublishedEvent.class));
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.update(2L, 10L,
                    new UpdatePostRequest("t", "c", false, List.of())))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 수정 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("작성자가 삭제하면 Redis 캐시도 함께 삭제한다")
        void success() {
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));

            postService.delete(1L, 10L);

            then(redisTemplate).should().delete("views:10");
            then(postRepository).should().delete(post);
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.delete(2L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 삭제 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("getPost")
    class GetPost {
        @Test
        @DisplayName("조회 시 Redis viewCount를 증가시킨다")
        void incrementsRedisViewCount() {
            given(postRepository.findByIdWithAuthor(10L)).willReturn(Optional.of(post));
            given(valueOperations.increment(anyString())).willReturn(1L);
            given(valueOperations.get(anyString())).willReturn("1");

            postService.getPost(10L);

            then(valueOperations).should().increment("views:10");
        }

        @Test
        @DisplayName("존재하지 않는 게시글이면 예외를 던진다")
        void notFound() {
            given(postRepository.findByIdWithAuthor(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPost(99L))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
