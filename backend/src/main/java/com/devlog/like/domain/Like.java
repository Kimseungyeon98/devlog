package com.devlog.like.domain;

import com.devlog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "target_id", "target_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Like {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LikeTarget targetType;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Like of(User user, Long targetId, LikeTarget targetType) {
        Like like = new Like();
        like.user = user;
        like.targetId = targetId;
        like.targetType = targetType;
        return like;
    }
}