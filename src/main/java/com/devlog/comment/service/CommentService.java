package com.devlog.comment.service;

import com.devlog.comment.domain.Comment;
import com.devlog.comment.domain.CommentClosure;
import com.devlog.comment.dto.request.CreateCommentRequest;
import com.devlog.comment.dto.request.UpdateCommentRequest;
import com.devlog.comment.dto.response.CommentResponse;
import com.devlog.comment.repository.CommentClosureRepository;
import com.devlog.comment.repository.CommentRepository;
import com.devlog.post.domain.Post;
import com.devlog.post.repository.PostRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

    // 댓글 작성
    @Transactional
    public Long create(Long userId, Long postId, CreateCommentRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.create(post, author, request.content());
        commentRepository.save(comment);

        if (request.parentId() == null) {
            // 최상위 댓글 — 자기 자신만 closure에 추가
            commentClosureRepository.save(CommentClosure.of(comment, comment, 0));
        } else {
            // 대댓글 — 부모의 모든 조상과 관계 추가
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글을 찾을 수 없습니다."));

            // 부모 댓글 기준 depth 확인 (1단계만 허용)
            List<CommentClosure> parentClosures = commentClosureRepository
                    .findByDescendantId(parent.getId());

            boolean isAlreadyChild = parentClosures.stream()
                    .anyMatch(cc -> cc.getDepth() == 1);

            if (isAlreadyChild) {
                throw new IllegalArgumentException("대댓글에는 댓글을 달 수 없습니다.");
            }

            // 자기 자신 closure
            commentClosureRepository.save(CommentClosure.of(comment, comment, 0));
            // 부모와의 관계 closure
            commentClosureRepository.save(CommentClosure.of(parent, comment, 1));
        }

        return comment.getId();
    }

    // 게시글 댓글 목록 조회
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

    // 댓글 수정
    @Transactional
    public void update(Long userId, Long commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.isAuthor(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        comment.update(request.content());
    }

    // 댓글 삭제 (소프트 딜리트)
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