package com.sprint.team2.monew.domain.article.saga;

import com.sprint.team2.monew.domain.article.collect.CollectorManager;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.service.basic.BasicArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleSagaOrchestrator {

    private final CollectorManager collectorManager;
    private final BasicArticleService basicArticleService;
    private final InterestRepository interestRepository;

    public void executeCollector(UUID interestId) {
        executeCollector(interestId, new SagaContext(UUID.randomUUID(), interestId));
    }

    public void executeCollector(UUID interestId, SagaContext sagaContext) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> InterestNotFoundException.notFound(interestId));

        try {
            List<String> keywords = interest.getKeywords();
            for (String keyword : keywords) {
                log.info("[Article|Saga] 관심사({}) 키워드 '{}'에 대해 수집 시작", interest.getName(), keyword);

                // 1. 네이버 기사 수집
                List<ArticleDto> naverArticles = executeStep(
                        sagaContext,
                        "collector-naver",
                        () -> collectorManager.collectFromNaver(keyword),
                        (result) -> log.info("[Article|Saga] collector-naver 보상 없음")
                );

                // 2. RSS 기사 수집
                List<ArticleDto> rssArticles = executeStep(
                        sagaContext,
                        "collector-rss",
                        () -> collectorManager.collectFromRss(keyword),
                        (result) -> log.info("[Article|Saga] collector-rss 보상 없음")
                );

                // 3. 병합 & DB 저장
                List<ArticleDto> merged = merge(naverArticles, rssArticles);
                executeStep(
                        sagaContext,
                        "save-articles-" + keyword,
                        () -> basicArticleService.saveAll(merged, interestId, keyword),
                        (result) -> basicArticleService.deleteAllByInterest(interestId, keyword)
                );

                sagaContext.completeSaga();
                log.info("[Article|Saga] SUCCESS | 뉴스 수집 완료: {}", sagaContext.getSagaId());
            }
        } catch (Exception e) {
            log.error("[Article|Saga] FAIL | 오류 발생, rollback 실행, {}", e.getMessage(), e);
            sagaContext.executeCompensations();
            throw e;
        }

    }

    private <T> T executeStep(SagaContext sagaContext,
                              String stepName,
                              Supplier<T> action,
                              Consumer<T> compensation) {

        sagaContext.startStep(stepName);
        try {
            T result = action.get();
            sagaContext.completeStep(stepName, result);

            if (compensation != null) {
                sagaContext.registerCompensation(() -> compensation.accept(result));
            }

            return result;
        } catch (Exception e) {
            log.error("[Article|Saga] Saga Step 실패: {}", stepName, e);
            throw e;
        }
    }

    private List<ArticleDto> merge(List<ArticleDto> a, List<ArticleDto> b) {
        return Stream.concat(a.stream(), b.stream())
                .distinct()
                .toList();
    }
}
