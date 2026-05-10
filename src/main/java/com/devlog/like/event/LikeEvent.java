package com.devlog.like.event;

import com.devlog.like.domain.LikeTarget;

public record LikeEvent(
        Long actorId,       // 좋아요 누른 사람
        Long targetOwnerId, // 좋아요 받은 사람
        Long targetId,
        LikeTarget targetType
) {}