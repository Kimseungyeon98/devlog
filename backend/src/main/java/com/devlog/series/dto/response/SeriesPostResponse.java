package com.devlog.series.dto.response;

import com.devlog.series.domain.SeriesPost;

import java.time.LocalDateTime;

public record SeriesPostResponse(
        Long id,
        Long postId,
        String title,
        int orderNum,
        LocalDateTime createdAt
) {
    public static SeriesPostResponse of(SeriesPost sp) {
        return new SeriesPostResponse(
                sp.getId(),
                sp.getPost().getId(),
                sp.getPost().getTitle(),
                sp.getOrderNum(),
                sp.getPost().getCreatedAt()
        );
    }
}
