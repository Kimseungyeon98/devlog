package com.devlog.user.controller;

import com.devlog.user.dto.request.SignUpRequest;
import com.devlog.user.dto.request.UpdateProfileRequest;
import com.devlog.user.dto.response.UserProfileResponse;
import com.devlog.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signUp(request));
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable String username,
            @AuthenticationPrincipal Long currentUserId) {
        return ResponseEntity.ok(userService.getProfile(username, currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getProfileById(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdateProfileRequest request) {
        userService.updateProfile(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{followingId}/follow")
    public ResponseEntity<Void> follow(
            @AuthenticationPrincipal Long followerId,
            @PathVariable Long followingId) {
        userService.follow(followerId, followingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{followingId}/follow")
    public ResponseEntity<Void> unfollow(
            @AuthenticationPrincipal Long followerId,
            @PathVariable Long followingId) {
        userService.unfollow(followerId, followingId);
        return ResponseEntity.noContent().build();
    }
}
