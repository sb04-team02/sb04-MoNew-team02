package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CollectorManager {

    private final NaverApiCollector naverCollector;
    private final RssCollector rssCollector;

    public List<ArticleDto> collectFromNaver(String keyword) {
        return naverCollector.collect(keyword);
    }

    public List<ArticleDto> collectFromRss(String keyword) {
        return rssCollector.collect(keyword);
    }
}
