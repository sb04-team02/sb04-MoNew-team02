package com.sprint.team2.monew.domain.article.service;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

    void saveByInterest(UUID interestId);

    List<ArticleDto> read(UUID userId, String orderBy, String direction, int limit);
}
