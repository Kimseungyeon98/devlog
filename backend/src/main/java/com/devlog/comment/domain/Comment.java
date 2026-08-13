package com.devlog.comment.domain;

import com.devlog.BaseTimeEntity;
import com.devlog.post.domain.Post;
import com.devlog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private boolean isDeleted = false;

    public static Comment create(Post post, User author, String content) {
        Comment comment = new Comment();
        comment.post = post;
        comment.author = author;
        comment.content = content;
        return comment;
    }

    public void update(String content) {
        this.content = content;
    }

    public void delete() {
        this.isDeleted = true;
        this.content = "삭제된 됓글입니다.";
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}