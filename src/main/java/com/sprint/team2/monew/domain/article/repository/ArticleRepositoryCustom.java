package com.sprint.team2.monew.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.team2.monew.domain.article.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Article> searchArticles(
            String keyword,
            UUID interestId,
            List<ArticleSource> sourceIn,
            LocalDateTime publishDateFrom,
            LocalDateTime publishDateTo,
            ArticleOrderBy orderBy,
            ArticleDirection direction,
            String cursor,
            LocalDateTime after,
            int limit
    ) {
        QArticle article = QArticle.article;
        BooleanBuilder builder = build(keyword, interestId, sourceIn, publishDateFrom, publishDateTo);

        if (cursor != null) {
            switch (orderBy) {
                case commentCount:
                    long commentCursor = Long.parseLong(cursor);
                    builder.and(direction == ArticleDirection.ASC ?
                            article.commentCount.gt(commentCursor) : article.commentCount.lt(commentCursor));
                    if (after != null)
                        builder.and(direction == ArticleDirection.ASC ?
                                article.createdAt.gt(after) : article.createdAt.lt(after));
                    break;

                case viewCount:
                    long viewCursor = Long.parseLong(cursor);
                    builder.and(direction == ArticleDirection.ASC ?
                            article.viewCount.gt(viewCursor) : article.viewCount.lt(viewCursor));
                    if (after != null)
                        builder.and(direction == ArticleDirection.ASC ?
                                article.createdAt.gt(after) : article.createdAt.lt(after));
                    break;

                default: // publishDate
                    LocalDateTime cursorDate = LocalDateTime.parse(cursor);
                    builder.and(direction == ArticleDirection.ASC ?
                            article.publishDate.gt(cursorDate) : article.publishDate.lt(cursorDate));
                    if (after != null)
                        builder.and(direction == ArticleDirection.ASC ?
                                article.createdAt.gt(after) : article.createdAt.lt(after));
                    break;
            }
        }

        var orderSpecifier = switch (orderBy) {
            case commentCount ->
                    direction == ArticleDirection.ASC ? article.commentCount.asc() : article.commentCount.desc();
            case viewCount -> direction == ArticleDirection.ASC ? article.viewCount.asc() : article.viewCount.desc();
            default -> direction == ArticleDirection.ASC ? article.publishDate.asc() : article.publishDate.desc();
        };

        return jpaQueryFactory
                .selectFrom(article)
                .where(builder)
                .orderBy(orderSpecifier, article.createdAt.asc())
                .limit(limit + 1)
                .fetch();
    }

    public long countArticles(
            String keyword,
            UUID interestId,
            List<ArticleSource> sourceIn,
            LocalDateTime publishDateFrom,
            LocalDateTime publishDateTo
    ) {
        QArticle article = QArticle.article;
        BooleanBuilder builder = build(keyword, interestId, sourceIn, publishDateFrom, publishDateTo);

        Long total = jpaQueryFactory
                .select(article.count())
                .from(article)
                .where(builder)
                .fetchOne();

        return total != null ? total : 0L;
    }

    // 공통 메서드
    private BooleanBuilder build(
            String keyword,
            UUID interestId,
            List<ArticleSource> sourceIn,
            LocalDateTime publishDateFrom,
            LocalDateTime publishDateTo
    ) {
        QArticle article = QArticle.article;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(article.deletedAt.isNull());

        if (keyword != null && !keyword.isBlank()) {
            builder.and(article.title.containsIgnoreCase(keyword)
                    .or(article.summary.containsIgnoreCase(keyword)));
        }
        if (interestId != null) {
            builder.and(article.interest.id.eq(interestId));
        }
        if (sourceIn != null && !sourceIn.isEmpty()) {
            builder.and(article.source.in(sourceIn));
        }

        if (publishDateFrom != null && publishDateTo != null) {
            builder.and(article.publishDate.between(publishDateFrom, publishDateTo));
        }
        if (publishDateFrom != null) {
            builder.and(article.publishDate.goe(publishDateFrom));
        }
        if (publishDateTo != null) {
            builder.and(article.publishDate.loe(publishDateTo));
        }

        return builder;
    }
}
