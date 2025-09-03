package com.sprint.team2.monew.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseArticleDto(
        List<ArticleDto> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
