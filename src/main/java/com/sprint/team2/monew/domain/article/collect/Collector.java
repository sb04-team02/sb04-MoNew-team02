package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;

import java.util.List;

public interface Collector {
    List<ArticleDto> collect(String keyword);
}
