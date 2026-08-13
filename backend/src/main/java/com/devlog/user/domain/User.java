package com.devlog.user.domain;

import com.devlog.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(nullable = false)
    private boolean isActive = true;

    public static User createLocalUser(String username, String email,
                                       String encodedPassword, String nickname) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.passwordHash = encodedPassword;
        user.nickname = nickname;
        return user;
    }

    public static User createOAuthUser(String username, String email, String nickname) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.nickname = nickname;
        return user;
    }

    public void updateProfile(String nickname, String bio, String profileImageUrl) {
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
