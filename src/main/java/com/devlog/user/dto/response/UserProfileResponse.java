package com.devlog.user.dto.response;

import com.devlog.user.domain.User;

public record UserProfileResponse(
        Long id,
        String username,
        String nickname,
        String bio,
        String profileImageUrl,
        long followerCount,
        long followingCount,
        Boolean isFollowing
) {
    public static UserProfileResponse of(User user, long followerCount, long followingCount) {
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getBio(), user.getProfileImageUrl(),
                followerCount, followingCount, null
        );
    }

    public static UserProfileResponse of(User user, long followerCount, long followingCount, boolean isFollowing) {
        return new UserProfileResponse(
                user.getId(), user.getUsername(), user.getNickname(),
                user.getBio(), user.getProfileImageUrl(),
                followerCount, followingCount, isFollowing
        );
    }
}
