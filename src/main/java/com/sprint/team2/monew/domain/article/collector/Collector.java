package com.sprint.team2.monew.domain.article.collector;

import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;

public interface Collector {
    CursorPageResponseArticleDto collect(String keyword, int size, String cursor);
}
