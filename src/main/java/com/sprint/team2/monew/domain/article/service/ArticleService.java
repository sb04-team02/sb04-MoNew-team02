package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

    void saveByInterest(UUID interestId);

    CursorPageResponseArticleDto read(UUID userId, ArticleOrderBy orderBy, ArticleDirection direction, int limit,
                                      String keyword,
                                      UUID interestId, List<ArticleSource> sourceIn, LocalDateTime publishedDateFrom, LocalDateTime publishedDateTo,
                                      String cursor, LocalDateTime after);

    void softDelete(UUID articleId);
}
