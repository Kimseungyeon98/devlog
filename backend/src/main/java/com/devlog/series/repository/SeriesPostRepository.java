package com.devlog.series.repository;

import com.devlog.series.domain.SeriesPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SeriesPostRepository extends JpaRepository<SeriesPost, Long> {

    Optional<SeriesPost> findBySeriesIdAndPostId(Long seriesId, Long postId);

    @Query("SELECT sp FROM SeriesPost sp " +
           "JOIN FETCH sp.series s " +
           "JOIN FETCH s.author " +
           "LEFT JOIN FETCH s.seriesPosts allSp " +
           "LEFT JOIN FETCH allSp.post " +
           "WHERE sp.post.id = :postId")
    Optional<SeriesPost> findByPostIdWithSeries(@Param("postId") Long postId);
}
