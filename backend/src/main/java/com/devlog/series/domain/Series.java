package com.devlog.series.domain;

import com.devlog.BaseTimeEntity;
import com.devlog.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Series extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderNum ASC")
    private List<SeriesPost> seriesPosts = new ArrayList<>();

    public static Series create(User author, String title, String description) {
        Series series = new Series();
        series.author = author;
        series.title = title;
        series.description = description;
        return series;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}
