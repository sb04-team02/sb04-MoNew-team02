package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.exception.NaverApiEmptyResponseException;
import com.sprint.team2.monew.domain.article.exception.NaverApiFailException;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NaverApiCollector implements Collector {
    private final WebClient webClient;
    private final ArticleMapper mapper;
    private final String clientId;
    private final String clientSecret;


    public NaverApiCollector(WebClient webClient,
                             ArticleMapper mapper,
                             @Value("${naver.api.naver-client-id}") String clientId,
                             @Value("${naver.api.naver-client-secret}") String clientSecret

    ) {
        this.webClient = webClient;
        this.mapper = mapper;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<ArticleDto> collect(String keyword) {
        Map<String, Object> response;
        try {
            response = webClient.get()
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

        } catch (WebClientResponseException e) {
            throw new NaverApiFailException("Naver API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            throw new NaverApiFailException("뉴스 수집 중 예외 발생: " + e.getMessage());
        }

        Object itemsObj = response != null ? response.get("items") : null;
        if (!(itemsObj instanceof List)) {
            log.warn("[Article] Naver API가 해당 키워드에 대해 빈 응답을 리턴함 (keyword: {})", keyword);
            throw new NaverApiEmptyResponseException();
        }

        List<Map<String, String>> items = (List<Map<String, String>>) itemsObj;

        return items.stream()
                .map(mapper::toArticleDto)
                .toList();
    }
}
