package com.sprint.team2.monew.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleRestoreResultDto(
        LocalDateTime restoreDate,
        List<String> restoredArticleIds,
        long restoredArticleCount
) {
}
