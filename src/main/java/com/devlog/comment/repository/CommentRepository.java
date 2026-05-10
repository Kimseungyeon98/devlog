package com.devlog.comment.repository;

import com.devlog.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 최상위 댓글 조회 (depth=0인 것만)
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.id IN (
            SELECT cc.descendant.id FROM CommentClosure cc
            WHERE cc.ancestor.id = cc.descendant.id
            AND cc.depth = 0
            AND c.post.id = :postId
        )
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

    // 특정 댓글의 대댓글 조회
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.author
        JOIN CommentClosure cc ON cc.descendant.id = c.id
        WHERE cc.ancestor.id = :parentId
        AND cc.depth = 1
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findChildComments(@Param("parentId") Long parentId);
}