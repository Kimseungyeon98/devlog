package com.devlog.comment.service;

import com.devlog.comment.domain.Comment;
import com.devlog.comment.domain.CommentClosure;
import com.devlog.comment.dto.request.CreateCommentRequest;
import com.devlog.comment.dto.request.UpdateCommentRequest;
import com.devlog.comment.dto.response.CommentResponse;
import com.devlog.comment.event.CommentEvent;
import com.devlog.comment.repository.CommentClosureRepository;
import com.devlog.comment.repository.CommentRepository;
import com.devlog.post.domain.Post;
import com.devlog.post.repository.PostRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentClosureRepository commentClosureRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(Long userId, Long postId, CreateCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.create(post, author, request.content());
        commentRepository.save(comment);

        if (request.parentId() == null) {
            commentClosureRepository.save(CommentClosure.of(comment, comment, 0));
            eventPublisher.publishEvent(new CommentEvent(
                    userId,
                    post.getAuthor().getId(),
                    null,
                    postId,
                    comment.getId()
            ));
        } else {
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));

            List<CommentClosure> parentClosures = commentClosureRepository
                    .findByDescendantId(parent.getId());

            boolean isAlreadyChild = parentClosures.stream()
                    .anyMatch(cc -> cc.getDepth() == 1);

            if (isAlreadyChild) {
                throw new IllegalArgumentException("대댓글에는 댓글을 달 수 없습니다.");
            }

            commentClosureRepository.save(CommentClosure.of(comment, comment, 0));
            commentClosureRepository.save(CommentClosure.of(parent, comment, 1));

            eventPublisher.publishEvent(new CommentEvent(
                    userId,
                    post.getAuthor().getId(),
                    parent.getAuthor().getId(),
                    postId,
                    comment.getId()
            ));
        }

        return comment.getId();
    }

    public List<CommentResponse> getComments(Long postId) {
        List<Comment> rootComments = commentRepository.findRootCommentsByPostId(postId);

        return rootComments.stream()
                .map(root -> {
                    List<Comment> children = commentRepository.findChildComments(root.getId());
                    List<CommentResponse> childResponses = children.stream()
                            .map(child -> CommentResponse.of(child, List.of()))
                            .toList();
                    return CommentResponse.of(root, childResponses);
                })
                .toList();
    }

    @Transactional
    public void update(Long userId, Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.update(request.content());
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        comment.delete();
    }
}
