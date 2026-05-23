package com.devlog.user.service;

import com.devlog.user.domain.Follow;
import com.devlog.user.domain.User;
import com.devlog.user.dto.request.SignUpRequest;
import com.devlog.user.dto.request.UpdateProfileRequest;
import com.devlog.user.dto.response.UserProfileResponse;
import com.devlog.user.repository.FollowRepository;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(SignUpRequest request) {
        validateDuplicate(request.email(), request.username());
        User user = User.createLocalUser(
                request.username(), request.email(),
                passwordEncoder.encode(request.password()), request.nickname()
        );
        return userRepository.save(user).getId();
    }

    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        long followerCount = followRepository.countByFollowing(user);
        long followingCount = followRepository.countByFollower(user);
        return UserProfileResponse.of(user, followerCount, followingCount);
    }

    public UserProfileResponse getProfile(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        long followerCount = followRepository.countByFollowing(user);
        long followingCount = followRepository.countByFollower(user);
        if (currentUserId == null) {
            return UserProfileResponse.of(user, followerCount, followingCount);
        }
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        boolean isFollowing = currentUser != null &&
                followRepository.existsByFollowerAndFollowing(currentUser, user);
        return UserProfileResponse.of(user, followerCount, followingCount, isFollowing);
    }

    public UserProfileResponse getProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        long followerCount = followRepository.countByFollowing(user);
        long followingCount = followRepository.countByFollower(user);
        return UserProfileResponse.of(user, followerCount, followingCount);
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        user.updateProfile(request.nickname(), request.bio(), request.profileImageUrl());
    }

    @Transactional
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
        }
        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("이미 팔로우하고 있습니다.");
        }
        followRepository.save(Follow.of(follower, following));
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();
        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    private void validateDuplicate(String email, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
    }
}
