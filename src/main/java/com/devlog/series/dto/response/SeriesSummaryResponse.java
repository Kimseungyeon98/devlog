package com.devlog.series.dto.response;

import com.devlog.series.domain.Series;

import java.time.LocalDateTime;

public record SeriesSummaryResponse(
        Long id,
        String title,
        String description,
        int postCount,
        LocalDateTime createdAt
) {
    public static SeriesSummaryResponse of(Series series) {
        return new SeriesSummaryResponse(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                series.getSeriesPosts().size(),
                series.getCreatedAt()
        );
    }
}
