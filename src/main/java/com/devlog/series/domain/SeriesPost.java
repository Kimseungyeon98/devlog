package com.devlog.series.domain;

import com.devlog.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "series_posts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"series_id", "post_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeriesPost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private int orderNum;

    public static SeriesPost of(Series series, Post post, int orderNum) {
        SeriesPost sp = new SeriesPost();
        sp.series = series;
        sp.post = post;
        sp.orderNum = orderNum;
        return sp;
    }

    public void updateOrder(int orderNum) {
        this.orderNum = orderNum;
    }
}
