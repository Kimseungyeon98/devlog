package com.devlog.user.repository;

import com.devlog.user.domain.Follow;
import com.devlog.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    long countByFollowing(User following);

    long countByFollower(User follower);

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :followingId")
    List<User> findFollowersByFollowingId(@Param("followingId") Long followingId);
}
