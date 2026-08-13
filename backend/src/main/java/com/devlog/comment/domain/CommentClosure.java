package com.devlog.comment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_closure")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentClosure {

    @EmbeddedId
    private CommentClosureId id;

    @MapsId("ancestorId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor")
    private Comment ancestor;

    @MapsId("descendantId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descendant")
    private Comment descendant;

    @Column(nullable = false)
    private int depth;

    public static CommentClosure of(Comment ancestor, Comment descendant, int depth) {
        CommentClosure closure = new CommentClosure();
        closure.id = new CommentClosureId(ancestor.getId(), descendant.getId());
        closure.ancestor = ancestor;
        closure.descendant = descendant;
        closure.depth = depth;
        return closure;
    }
}