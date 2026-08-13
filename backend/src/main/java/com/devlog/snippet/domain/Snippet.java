package com.devlog.snippet.domain;

import com.devlog.BaseTimeEntity;
import com.devlog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "snippets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Snippet extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String code;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Long viewCount = 0L;

    public static Snippet create(User author, String title, String description, String code, String language) {
        Snippet s = new Snippet();
        s.author = author;
        s.title = title;
        s.description = description;
        s.code = code;
        s.language = language;
        return s;
    }

    public void update(String title, String description, String code, String language) {
        this.title = title;
        this.description = description;
        this.code = code;
        this.language = language;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}
