package com.devlog.post.repository;

import com.devlog.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithAuthor(@Param("id") Long id);

    // ← fetch join으로 author 한 번에 조회
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.author
        WHERE p.author.id = :authorId
        ORDER BY p.createdAt DESC
    """)
    List<Post> findByAuthorIdWithAuthor(@Param("authorId") Long authorId);
}
