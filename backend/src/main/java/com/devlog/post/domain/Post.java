package com.devlog.post.domain;

import com.devlog.BaseTimeEntity;
import com.devlog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable=false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.DRAFT;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    public static Post create(User author, String title, String content) {
        Post post = new Post();
        post.author = author;
        post.title = title;
        post.content = content;
        return post;
    }

    public void publish() {
        this.status = PostStatus.PUBLISHED;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void updateViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }

    public void addTag(Tag tag) {
        PostTag postTag = PostTag.of(this, tag);
        this.postTags.add(postTag);
    }

    public void clearTags() {
        this.postTags.clear();
    }
}
