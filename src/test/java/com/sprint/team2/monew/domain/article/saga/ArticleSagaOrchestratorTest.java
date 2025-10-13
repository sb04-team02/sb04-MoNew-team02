package com.sprint.team2.monew.domain.article.saga;

import com.sprint.team2.monew.domain.article.collect.CollectorManager;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.service.basic.BasicArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleSagaOrchestratorTest {

    @InjectMocks
    private ArticleSagaOrchestrator orchestrator;
    @InjectMocks
    private SagaContext sagaContext;

    @Mock
    private InterestRepository interestRepository;
    @Mock
    private CollectorManager collectorManager;
    @Mock
    private BasicArticleService articleService;

    @Test
    @DisplayName("모든 단계를 성공한 정상 플로우")
    void ExecuteCollectorSuccess() {
        // given
        UUID interestId = UUID.randomUUID();
        Interest interest = new Interest("스포츠", List.of("축구", "야구", "농구"));
        Article article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl("source url")
                .title("title")
                .publishDate(LocalDateTime.now())
                .summary("summary")
                .commentCount(10)
                .viewCount(10)
                .build();

        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(collectorManager.collectFromNaver(anyString())).thenReturn(List.of(
                new ArticleDto(
                        UUID.randomUUID(),
                        "sample source",
                        "sample source url",
                        "sample title",
                        LocalDateTime.now(),
                        "sample summary",
                        5,
                        10,
                        true
                )));
        when(collectorManager.collectFromRss(anyString())).thenReturn(List.of(
                new ArticleDto(
                        UUID.randomUUID(),
                        "sample source 2",
                        "sample source url 2",
                        "sample title 2",
                        LocalDateTime.now(),
                        "sample summary 2",
                        50,
                        100,
                        false
                )));
        when(articleService.saveAll(anyList(), eq(interestId), anyString())).thenReturn(List.of(article));

        // when
        orchestrator.executeCollector(interestId, sagaContext);

        // then
        verify(articleService, times(3)).saveAll(anyList(), eq(interestId), anyString());
        verify(articleService, never()).deleteAllByInterest(any(), any());
    }

    @Test
    @DisplayName("네이버 뉴스 기사 수집 실패 후 롤백 수행")
    void executeCollectorFailWhenNaverApiErrorThenRollback() {
        // given
        UUID interestId = UUID.randomUUID();
        Interest interest = new Interest("스포츠", List.of("축구", "야구", "농구"));
        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(collectorManager.collectFromNaver(anyString())).thenThrow(new RuntimeException("네이버 기사 수집 중 오류 발생"));

        SagaContext spySagaContext = spy(new SagaContext(UUID.randomUUID(), interestId));

        // when
        assertThrows(RuntimeException.class, () -> orchestrator.executeCollector(interestId, spySagaContext));

        // then
        verify(spySagaContext, times(1)).executeCompensations();
    }

    @Test
    @DisplayName("DB 저장 오류 후 롤백 수행")
    void executeCollectorFailWhenDbErrorThenRollback() {
        // given
        UUID interestId = UUID.randomUUID();
        Interest interest = new Interest("스포츠", List.of("축구", "야구", "농구"));
        when(interestRepository.findById(interestId)).thenReturn(Optional.of(interest));
        when(collectorManager.collectFromNaver(anyString())).thenReturn(List.of(
                new ArticleDto(
                        UUID.randomUUID(),
                        "sample source",
                        "sample source url",
                        "sample title",
                        LocalDateTime.now(),
                        "sample summary",
                        5,
                        10,
                        true
                )));
        when(collectorManager.collectFromRss(anyString())).thenReturn(List.of(
                new ArticleDto(
                        UUID.randomUUID(),
                        "sample source 2",
                        "sample source url 2",
                        "sample title 2",
                        LocalDateTime.now(),
                        "sample summary 2",
                        50,
                        100,
                        false
                )));
        when(articleService.saveAll(anyList(), eq(interestId), anyString())).thenThrow(new RuntimeException("DB 저장 오류"));

        // when
        assertThrows(RuntimeException.class, () -> orchestrator.executeCollector(interestId, sagaContext));

        // then
        verify(articleService, never()).deleteAllByInterest(eq(interestId), anyString());
    }
}