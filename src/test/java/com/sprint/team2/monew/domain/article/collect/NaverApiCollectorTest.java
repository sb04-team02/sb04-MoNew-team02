package com.sprint.team2.monew.domain.article.collect;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.exception.NaverApiEmptyResponseException;
import com.sprint.team2.monew.domain.article.exception.NaverApiFailException;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaverApiCollectorTest {

    private WebClient webClient;
    private ArticleMapper mapper;
    private NaverApiCollector collector;

    @BeforeEach
    void setUp() {
        webClient = mock(WebClient.class);
        mapper = mock(ArticleMapper.class);
        collector = new NaverApiCollector(webClient, mapper, "id", "secret");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("키워드로 뉴스 기사 수집 시 ArticleDto 리스트 반환")
    void collectShouldReturnArticles() {
        // given
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        given(webClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(headersSpec);
        given(headersSpec.header(any(), any())).willReturn(headersSpec);
        given(headersSpec.retrieve()).willReturn(responseSpec);

        List<Map<String, String>> items = List.of(
                Map.of("title", "뉴스1", "link", "http://news1.com", "summary", "요약1"),
                Map.of("title", "뉴스2", "link", "http://news2.com", "summary", "요약2")
        );

        Map<String, Object> fakeResponse = Map.of("items", items);

        given(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .willReturn(Mono.just(fakeResponse));

        given(mapper.toArticleDto(any(Map.class))).willAnswer(invocation -> {
            Map<String, String> item = invocation.getArgument(0);
            return new ArticleDto(
                    UUID.randomUUID(),
                    "NAVER",
                    item.get("link"),
                    item.get("title"),
                    LocalDateTime.now(),
                    item.get("summary"),
                    0L,
                    0L,
                    false
            );
        });

        // when
        List<ArticleDto> result = collector.collect("엔비디아");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("뉴스1");
        assertThat(result.get(1).title()).isEqualTo("뉴스2");

        then(mapper).should(times(2)).toArticleDto(any(Map.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("API 응답이 비어있으면 NaverApiEmptyResponseException 발생")
    void collectShouldThrowEmptyResponseException() {
        // given
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        given(webClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(headersSpec);
        given(headersSpec.header(any(), any())).willReturn(headersSpec);
        given(headersSpec.retrieve()).willReturn(responseSpec);

        // items 키는 있는데 값이 잘못된 케이스
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("items", null);

        given(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .willReturn(Mono.just(emptyResponse));

        // when & then
        assertThatThrownBy(() -> collector.collect("엔비디아"))
                .isInstanceOf(NaverApiEmptyResponseException.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    @DisplayName("WebClient 호출 실패 시 NaverApiFailException 발생")
    void collectShouldThrowFailExceptionOnWebClientError() {
        // given
        WebClient.RequestHeadersUriSpec uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        given(webClient.get()).willReturn(uriSpec);
        given(uriSpec.uri(any(java.util.function.Function.class))).willReturn(headersSpec);
        given(headersSpec.header(any(), any())).willReturn(headersSpec);
        given(headersSpec.retrieve()).willReturn(responseSpec);

        given(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .willThrow(WebClientResponseException.create(500, "Internal Error", null, null, null));

        // when & then
        assertThatThrownBy(() -> collector.collect("엔비디아"))
                .isInstanceOf(NaverApiFailException.class);
    }
}