package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

    void saveByInterest(UUID interestId);

    CursorPageResponseArticleDto read(UUID userId, String orderBy, String direction, int limit,
                                      String keyword,
                                      UUID interestId, List<String> sourceIn, LocalDateTime publishedDateFrom, LocalDateTime publishedDateTo,
                                      String cursor, LocalDateTime after);
}
