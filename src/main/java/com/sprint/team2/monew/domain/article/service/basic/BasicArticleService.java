package com.sprint.team2.monew.domain.article.service.basic;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.team2.monew.domain.article.collect.NaverApiCollector;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.QArticle;
import com.sprint.team2.monew.domain.article.exception.ArticleCollectFailedException;
import com.sprint.team2.monew.domain.article.exception.ArticleSaveFailedException;
import com.sprint.team2.monew.domain.article.exception.InvalidParameterException;
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
    private final JPAQueryFactory jpaQueryFactory;

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
                    } catch (Exception e) {
                        log.error("[Article] 뉴스 기사 저장 실패", e);
                        throw ArticleSaveFailedException.articleSaveFailed();
                    }
                }
                log.info("[Article] keyword({})로 뉴스 수집 및 저장 성공", keyword);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto read(UUID userId, String orderBy, String direction, int limit,
                                             String keyword,
                                             UUID interestId, List<String> sourceIn, LocalDateTime publishedDateFrom, LocalDateTime publishedDateTo,
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

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (!articles.isEmpty()) {
            Article last = articles.get(articles.size() - 1);
            switch (orderBy) {
                case "commentCount" -> {
                    nextCursor = String.valueOf(last.getCommentCount());
                    nextAfter = last.getCreatedAt();
                }
                case "viewCount" -> {
                    nextCursor = String.valueOf(last.getViewCount());
                    nextAfter = last.getCreatedAt();
                }
                default -> {
                    nextCursor = last.getPublishDate().toString();
                    nextAfter = last.getPublishDate();
                }
            }
        }

        QArticle article = QArticle.article;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(article.title.containsIgnoreCase(keyword)
                    .or(article.summary.containsIgnoreCase(keyword)));
        }
        if (interestId != null) builder.and(article.interest.id.eq(interestId));
        if (sourceIn != null && !sourceIn.isEmpty()) builder.and(article.source.in(sourceIn));
        if (publishedDateFrom != null && publishedDateTo != null)
            builder.and(article.publishDate.between(publishedDateFrom, publishedDateTo));

        Long totalElementsResult = jpaQueryFactory
                .select(article.count())
                .from(article)
                .where(builder)
                .fetchOne();

        long totalElements = (totalElementsResult != null) ? totalElementsResult : 0L;

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
