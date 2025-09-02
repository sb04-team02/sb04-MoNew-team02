package com.sprint.team2.monew.domain.article.dto.response;


import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleDto(
        UUID id,
        String source,
        String sourceUrl,
        String title,
        LocalDateTime publishDate,
        String summary,
        long commentCount,
        long viewCount,
        boolean viewedByMe
) {
}
