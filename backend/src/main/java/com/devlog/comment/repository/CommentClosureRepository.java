package com.devlog.comment.repository;

import com.devlog.comment.domain.CommentClosure;
import com.devlog.comment.domain.CommentClosureId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentClosureRepository extends JpaRepository<CommentClosure, CommentClosureId> {

    // 특정 됓글의 모든 closure 조회 (삭제 시 사용)
    @Query("SELECT cc FROM CommentClosure cc WHERE cc.descendant.id = :commentId")
    List<CommentClosure> findByDescendantId(@Param("commentId") Long commentId);
}