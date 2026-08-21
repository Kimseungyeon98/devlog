package com.devlog.comment.service;

import com.devlog.comment.domain.Comment;
import com.devlog.comment.domain.CommentClosure;
import com.devlog.comment.dto.request.CreateCommentRequest;
import com.devlog.comment.dto.request.UpdateCommentRequest;
import com.devlog.comment.event.CommentEvent;
import com.devlog.comment.repository.CommentClosureRepository;
import com.devlog.comment.repository.CommentRepository;
import com.devlog.post.domain.Post;
import com.devlog.post.repository.PostRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock CommentClosureRepository commentClosureRepository;
    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @InjectMocks CommentService commentService;

    User postAuthor;
    User commenter;
    Post post;
    Comment rootComment;

    @BeforeEach
    void setUp() {
        postAuthor = User.createLocalUser("author", "author@test.com", "hash", "글쓴이");
        ReflectionTestUtils.setField(postAuthor, "id", 1L);

        commenter = User.createLocalUser("commenter", "c@test.com", "hash", "됓글러");
        ReflectionTestUtils.setField(commenter, "id", 2L);

        post = Post.create(postAuthor, "글제목", "글내용");
        ReflectionTestUtils.setField(post, "id", 10L);

        rootComment = Comment.create(post, commenter, "루트 됓글");
        ReflectionTestUtils.setField(rootComment, "id", 100L);
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("루트 됓글 작성 시 CommentEvent가 발행된다")
        void createRootComment() {
            given(userRepository.findById(2L)).willReturn(Optional.of(commenter));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(rootComment);

            commentService.create(2L, 10L, new CreateCommentRequest("루트 됓글", null));

            ArgumentCaptor<CommentEvent> captor = ArgumentCaptor.forClass(CommentEvent.class);
            then(eventPublisher).should().publishEvent(captor.capture());
            assertThat(captor.getValue().actorId()).isEqualTo(2L);
            assertThat(captor.getValue().postAuthorId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("대됓글 작성 시 부모의 closure 레코드가 추가된다")
        void createChildComment() {
            Comment child = Comment.create(post, commenter, "대됓글");
            ReflectionTestUtils.setField(child, "id", 101L);

            given(userRepository.findById(2L)).willReturn(Optional.of(commenter));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(child);
            given(commentRepository.findById(100L)).willReturn(Optional.of(rootComment));
            // rootComment는 루트이므로 depth=0 closure만 존재
            CommentClosure selfClosure = CommentClosure.of(rootComment, rootComment, 0);
            given(commentClosureRepository.findByDescendantId(100L)).willReturn(List.of(selfClosure));

            commentService.create(2L, 10L, new CreateCommentRequest("대됓글", 100L));

            // closure 저장 횟수: 자기자신(depth=0) + 부모연결(depth=1) = 2번
            then(commentClosureRepository).should(org.mockito.Mockito.times(2))
                    .save(any(CommentClosure.class));
        }

        @Test
        @DisplayName("3단계 중첩은 허용하지 않는다 (대됓글에 됓글 불가)")
        void noThirdLevelNesting() {
            Comment childComment = Comment.create(post, commenter, "대됓글");
            ReflectionTestUtils.setField(childComment, "id", 101L);

            given(userRepository.findById(2L)).willReturn(Optional.of(commenter));
            given(postRepository.findById(10L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(childComment);
            given(commentRepository.findById(101L)).willReturn(Optional.of(childComment));
            // childComment는 depth=1 closure를 가짐 → 3단계 시도
            CommentClosure parentClosure = CommentClosure.of(rootComment, childComment, 1);
            given(commentClosureRepository.findByDescendantId(101L)).willReturn(List.of(parentClosure));

            assertThatThrownBy(() -> commentService.create(2L, 10L,
                    new CreateCommentRequest("3단계 됓글", 101L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("대됓글에는 됓글을 달 수 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 게시글이면 예외를 던진다")
        void postNotFound() {
            given(userRepository.findById(2L)).willReturn(Optional.of(commenter));
            given(postRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.create(2L, 99L,
                    new CreateCommentRequest("됓글", null)))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("작성자가 수정하면 내용이 변경된다")
        void success() {
            given(commentRepository.findById(100L)).willReturn(Optional.of(rootComment));

            commentService.update(2L, 100L, new UpdateCommentRequest("수정된 됓글"));

            assertThat(rootComment.getContent()).isEqualTo("수정된 됓글");
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(commentRepository.findById(100L)).willReturn(Optional.of(rootComment));

            assertThatThrownBy(() -> commentService.update(1L, 100L,
                    new UpdateCommentRequest("수정")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("됓글 수정 권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("작성자가 삭제하면 소프트 딘리트된다")
        void success() {
            given(commentRepository.findById(100L)).willReturn(Optional.of(rootComment));

            commentService.delete(2L, 100L);

            assertThat(rootComment.isDeleted()).isTrue();
            assertThat(rootComment.getContent()).isEqualTo("삭제된 됓글입니다.");
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(commentRepository.findById(100L)).willReturn(Optional.of(rootComment));

            assertThatThrownBy(() -> commentService.delete(1L, 100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("됓글 삭제 권한이 없습니다.");
        }
    }
}
