package com.devlog.notification.controller;

import com.devlog.notification.dto.response.NotificationResponse;
import com.devlog.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    // 읽지 않은 알림 수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    // 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> read(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long notificationId) {
        notificationService.read(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}