package com.devlog.snippet.repository;

import com.devlog.snippet.domain.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    @Query("SELECT s FROM Snippet s JOIN FETCH s.author " +
           "WHERE (:language IS NULL OR s.language = :language) " +
           "AND (:keyword IS NULL OR s.title LIKE %:keyword% OR s.description LIKE %:keyword%)")
    Page<Snippet> search(@Param("keyword") String keyword,
                         @Param("language") String language,
                         Pageable pageable);
}
