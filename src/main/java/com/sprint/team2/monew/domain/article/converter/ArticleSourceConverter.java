package com.sprint.team2.monew.domain.article.converter;

import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArticleSourceConverter implements Converter<String, ArticleSource> {
    @Override
    public ArticleSource convert(String source) {
        return ArticleSource.from(source);
    }
}
