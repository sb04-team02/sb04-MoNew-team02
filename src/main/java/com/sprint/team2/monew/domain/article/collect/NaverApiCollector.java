package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
public class NaverApiCollector implements Collector {
    private final WebClient webClient;
    private final ArticleMapper mapper;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;


    public NaverApiCollector(WebClient webClient,
                             ArticleMapper mapper,
                             @Value("${naver.api.base-url}") String baseUrl,
                             @Value("${naver.api.naver-client-id}") String clientId,
                             @Value("${naver.api.naver-client-secret}") String clientSecret

    ) {
        this.webClient = webClient;
        this.mapper = mapper;
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<ArticleDto> collect(String keyword) {
        Map<String, Object> response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v1/search/news.json")
                                .queryParam("query", keyword)
                                .queryParam("display", 10)
                                .queryParam("sort", "date")
                                .build())
                        .header("X-Naver-Client-Id", clientId)
                        .header("X-Naver-Client-Secret", clientSecret)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        })
                        .block();

        List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");

        return items.stream()
                .map(mapper::toArticleDto)
                .toList();
    }
}
