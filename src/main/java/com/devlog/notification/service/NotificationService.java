package com.devlog.notification.service;

import com.devlog.comment.event.CommentEvent;
import com.devlog.like.event.LikeEvent;
import com.devlog.notification.domain.Notification;
import com.devlog.notification.domain.NotificationType;
import com.devlog.notification.dto.response.NotificationResponse;
import com.devlog.notification.repository.NotificationRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final long SSE_TIMEOUT = 600_000L;

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(userId);
        }
        return emitter;
    }

    @Async
    @EventListener
    @Transactional
    public void handleLikeEvent(LikeEvent event) {
        if (event.actorId().equals(event.targetOwnerId())) return;

        User owner = userRepository.findById(event.targetOwnerId()).orElse(null);
        if (owner == null) return;

        NotificationType type = event.targetType().name().equals("POST")
                ? NotificationType.LIKE_POST
                : NotificationType.LIKE_COMMENT;

        String content = event.targetType().name().equals("POST")
                ? "게시글에 좋아요가 달렸습니다."
                : "댓글에 좋아요가 달렸습니다.";

        Notification notification = notificationRepository.save(
                Notification.of(owner, content, type, event.targetId())
        );

        pushToEmitter(event.targetOwnerId(), NotificationResponse.of(notification));
    }

    @Async
    @EventListener
    @Transactional
    public void handleCommentEvent(CommentEvent event) {
        if (event.parentAuthorId() == null) {
            // 최상위 댓글 → 게시글 작성자에게 알림
            if (event.actorId().equals(event.postAuthorId())) return;
            User postAuthor = userRepository.findById(event.postAuthorId()).orElse(null);
            if (postAuthor == null) return;
            Notification notification = notificationRepository.save(
                    Notification.of(postAuthor, "게시글에 새 댓글이 달렸습니다.", NotificationType.COMMENT, event.postId())
            );
            pushToEmitter(event.postAuthorId(), NotificationResponse.of(notification));
        } else {
            // 대댓글 → 부모 댓글 작성자에게 알림
            if (event.actorId().equals(event.parentAuthorId())) return;
            User parentAuthor = userRepository.findById(event.parentAuthorId()).orElse(null);
            if (parentAuthor == null) return;
            Notification notification = notificationRepository.save(
                    Notification.of(parentAuthor, "댓글에 답글이 달렸습니다.", NotificationType.COMMENT_REPLY, event.postId())
            );
            pushToEmitter(event.parentAuthorId(), NotificationResponse.of(notification));
        }
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::of)
                .toList();
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void read(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다."));
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("알림 읽기 권한이 없습니다.");
        }
        notification.read();
    }

    private void pushToEmitter(Long userId, NotificationResponse response) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name("notification").data(response));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 userId={}", userId);
            emitters.remove(userId);
        }
    }
}
