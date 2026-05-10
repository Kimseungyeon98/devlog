package com.devlog.notification.service;

import com.devlog.like.event.LikeEvent;
import com.devlog.notification.repository.NotificationRepository;
import com.devlog.notification.domain.Notification;
import com.devlog.notification.domain.NotificationType;
import com.devlog.notification.dto.response.NotificationResponse;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // LikeEvent 수신 — 비동기 처리
    @Async
    @EventListener
    @Transactional
    public void handleLikeEvent(LikeEvent event) {
        // 자기 자신에게 좋아요 시 알림 X
        if (event.actorId().equals(event.targetOwnerId())) return;

        User owner = userRepository.findById(event.targetOwnerId()).orElse(null);
        if (owner == null) return;

        NotificationType type = event.targetType().name().equals("POST")
                ? NotificationType.LIKE_POST
                : NotificationType.LIKE_COMMENT;

        String content = event.targetType().name().equals("POST")
                ? "게시글에 좋아요가 달렸습니다."
                : "댓글에 좋아요가 달렸습니다.";

        notificationRepository.save(
                Notification.of(owner, content, type, event.targetId())
        );
    }

    // 알림 목록 조회
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::of)
                .toList();
    }

    // 읽지 않은 알림 수
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // 알림 읽음 처리
    @Transactional
    public void read(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림 읽기 권한이 없습니다.");
        }

        notification.read();
    }
}