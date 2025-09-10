package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.collect.NaverApiCollector;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.repository.ArticleRepositoryCustom;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BasicArticleService implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final ArticleRepositoryCustom articleRepositoryCustom;
    private final NaverApiCollector naverApiCollector;

    private final InterestRepository interestRepository;

    @Override
    public void saveByInterest(UUID interestId) {

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> InterestNotFoundException.notFound(interestId));
        for (String keyword : interest.getKeywords()) {
            List<ArticleDto> articles;
            try {
                articles = naverApiCollector.collect(keyword);
            } catch (RuntimeException e) {
                log.error("[Article] keyword({}) 수집 실패", keyword, e);
                throw ArticleCollectFailedException.withKeyword(keyword);
            }

            for (ArticleDto dto : articles) {
                if (!articleRepository.existsBySourceUrl(dto.sourceUrl())) {
                    Article articleEntity = articleMapper.toEntity(dto);
                    try {
                        articleRepository.save(articleEntity);
                        log.info("[Article] {}에서 keyword({}) 저장 성공: {} - {}",
                                dto.source(), keyword, dto.title(), dto.sourceUrl());
                    } catch (Exception e) {
                        log.error("[Article] {}에서 keyword({}) 저장 실패: {} - {}",
                                dto.source(), keyword, dto.title(), dto.sourceUrl(), e);
                        throw ArticleSaveFailedException.articleSaveFailed();
                    }
                } else {
                    log.debug("[Article] {}에서 keyword({}) 저장 실패(중복 기사): {} - {}",
                            dto.source(), keyword, dto.title(), dto.sourceUrl());
                }
            }
            log.info("[Article] keyword({})로 뉴스 수집 완료, total = {}", keyword, articles.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto read(UUID userId, ArticleOrderBy orderBy, ArticleDirection direction, int limit,
                                             String keyword,
                                             UUID interestId, List<ArticleSource> sourceIn, LocalDateTime publishedDateFrom, LocalDateTime publishedDateTo,
                                             String cursor, LocalDateTime after) {

        if (limit <= 0) {
            log.error("[Article] 커서 페이지 크기는 0보다 커야 함, limit = {}", limit);
            throw InvalidParameterException.invalidParameter();
        }

        if (!List.of("publishDate", "commentCount", "viewCount").contains(orderBy)) {
            log.error("[Article] 정렬 속성에는 publishDate, commentCount, viewCount만 가능, 정렬 속성 = {}", orderBy);
            throw InvalidParameterException.invalidParameter();
        }

        List<Article> articles = articleRepositoryCustom.searchArticles(
                keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo,
                orderBy, direction,
                cursor, after, limit
        );

        boolean hasNext = articles.size() > limit;
        if (hasNext) {
            articles = articles.subList(0, limit);
        }

        log.debug("[Article] 조회한 결과 조회, userId = {}, keyword = {}, interestId = {}, size = {}, hasNext = {}, nextCurosr = {}",
                userId, keyword, interestId, articles.size(), hasNext,
                !articles.isEmpty() ? (orderBy == ArticleOrderBy.publishDate ? articles.get(articles.size() - 1).getPublishDate() : articles.get(articles.size() - 1).getCommentCount()) : null);

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (!articles.isEmpty()) {
            Article last = articles.get(articles.size() - 1);
            switch (orderBy) {
                case commentCount -> nextCursor = String.valueOf(last.getCommentCount());
                case viewCount -> nextCursor = String.valueOf(last.getViewCount());
                default -> nextCursor = last.getPublishDate().toString();
            }

            nextAfter = last.getCreatedAt();
        }

        long totalElements = articleRepositoryCustom.countArticles(
                keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo
        );

        return new CursorPageResponseArticleDto(
                articles.stream().map(articleMapper::toArticleDto).toList(),
                nextCursor,
                nextAfter,
                articles.size(),
                totalElements,
                hasNext
        );
    }
}
