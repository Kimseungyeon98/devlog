package com.devlog.series.dto.response;

import com.devlog.series.domain.Series;

import java.time.LocalDateTime;
import java.util.List;

public record SeriesResponse(
        Long id,
        String title,
        String description,
        String authorUsername,
        String authorNickname,
        List<SeriesPostResponse> posts,
        LocalDateTime createdAt
) {
    public static SeriesResponse of(Series series) {
        return new SeriesResponse(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                series.getAuthor().getUsername(),
                series.getAuthor().getNickname(),
                series.getSeriesPosts().stream().map(SeriesPostResponse::of).toList(),
                series.getCreatedAt()
        );
    }
}
