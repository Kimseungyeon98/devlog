package com.devlog.user.repository;

import com.devlog.user.domain.Follow;
import com.devlog.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    long countByFollowing(User following);

    long countByFollower(User follower);

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);
}