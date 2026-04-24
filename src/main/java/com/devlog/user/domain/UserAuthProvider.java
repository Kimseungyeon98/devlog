package com.devlog.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_auth_providers",
        uniqueConstraints = @UniqueConstraint(
            columnNames = {"provider", "provider_user_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthProvider {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String provider; // GITHUB, GOOGLE

    @Column(nullable = false)
    private String providerUserId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static UserAuthProvider of(User user, String provider, String providerUserId) {
        UserAuthProvider auth = new UserAuthProvider();
        auth.user = user;
        auth.provider = provider;
        auth.providerUserId = providerUserId;
        return auth;
    }
}