package com.sprint.team2.monew.domain.article.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
        LocalDateTime restoreDate,
        List<UUID> restoredArticleIds,
        long restoredArticleCount
) {
}
