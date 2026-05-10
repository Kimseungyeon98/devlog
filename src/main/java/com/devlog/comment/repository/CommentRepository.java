package com.devlog.comment.repository;

import com.devlog.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // author fetch join으로 N+1 방지
    @Query("""
        SELECT c FROM Comment c
        JOIN FETCH c.author
        WHERE c.post.id = :postId
        AND c.id IN (
            SELECT cc.descendant.id FROM CommentClosure cc
            WHERE cc.ancestor.id = cc.descendant.id
            AND cc.depth = 0
        )
        ORDER BY c.createdAt ASC
    """)
    List<Comment> findRootCommentsByPostId(@Param("postId") Long postId);

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