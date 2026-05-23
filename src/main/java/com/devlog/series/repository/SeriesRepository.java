package com.devlog.series.repository;

import com.devlog.series.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeriesRepository extends JpaRepository<Series, Long> {

    @Query("SELECT s FROM Series s JOIN FETCH s.author WHERE s.author.username = :username ORDER BY s.createdAt DESC")
    List<Series> findByAuthorUsernameWithAuthor(@Param("username") String username);

    @Query("SELECT DISTINCT s FROM Series s " +
           "JOIN FETCH s.author " +
           "LEFT JOIN FETCH s.seriesPosts sp " +
           "LEFT JOIN FETCH sp.post " +
           "WHERE s.id = :id")
    Optional<Series> findByIdWithDetails(@Param("id") Long id);
}
