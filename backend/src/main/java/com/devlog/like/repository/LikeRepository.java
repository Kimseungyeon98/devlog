package com.devlog.like.repository;

import com.devlog.like.domain.Like;
import com.devlog.like.domain.LikeTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUserIdAndTargetIdAndTargetType(
            Long userId, Long targetId, LikeTarget targetType);

    boolean existsByUserIdAndTargetIdAndTargetType(
            Long userId, Long targetId, LikeTarget targetType);

    long countByTargetIdAndTargetType(Long targetId, LikeTarget targetType);
}