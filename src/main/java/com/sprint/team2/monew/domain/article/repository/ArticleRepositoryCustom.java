package com.sprint.team2.monew.domain.article.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.QArticle;
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
            List<String> sourceIn,
            LocalDateTime publishedDateFrom,
            LocalDateTime publishedDateTo,
            String orderBy,
            String direction,
            String cursor,
            LocalDateTime after,
            int limit
    ) {
        QArticle article = QArticle.article;
        BooleanBuilder builder = new BooleanBuilder();

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

        if (publishedDateFrom != null && publishedDateTo != null) {
            builder.and(article.publishDate.between(publishedDateFrom, publishedDateTo));
        }

        if (cursor != null) {
            switch (orderBy) {
                case "commentCount" -> {
                    long cursorValue = Long.parseLong(cursor);
                    if (after != null) {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.commentCount.gt(cursorValue).or(article.commentCount.eq(cursorValue).and(article.createdAt.gt(after))) :
                                article.commentCount.lt(cursorValue).or(article.commentCount.eq(cursorValue).and(article.createdAt.lt(after))));
                    } else {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.commentCount.gt(cursorValue) :
                                article.commentCount.lt(cursorValue));
                    }
                }

                case "viewCount" -> {
                    long cursorValue = Long.parseLong(cursor);
                    if (after != null) {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.viewCount.gt(cursorValue)
                                        .or(article.viewCount.eq(cursorValue).and(article.createdAt.gt(after))) :
                                article.viewCount.lt(cursorValue)
                                        .or(article.viewCount.eq(cursorValue).and(article.createdAt.lt(after))));
                    } else {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.viewCount.gt(cursorValue) :
                                article.viewCount.lt(cursorValue));
                    }
                }

                default -> {
                    LocalDateTime cursorDate = LocalDateTime.parse(cursor);
                    if (after != null) {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.publishDate.gt(cursorDate)
                                        .or(article.publishDate.eq(cursorDate).and(article.createdAt.gt(after))) :
                                article.publishDate.lt(cursorDate)
                                        .or(article.publishDate.eq(cursorDate).and(article.createdAt.lt(after))));
                    } else {
                        builder.and(direction.equalsIgnoreCase("ASC") ?
                                article.publishDate.gt(cursorDate) :
                                article.publishDate.lt(cursorDate));
                    }
                }
            }
        }

        var orderSpecifier = switch (orderBy) {
            case "commentCount" ->
                    direction.equalsIgnoreCase("ASC") ? article.commentCount.asc() : article.commentCount.desc();
            case "viewCount" -> direction.equalsIgnoreCase("ASC") ? article.viewCount.asc() : article.viewCount.desc();
            default -> direction.equalsIgnoreCase("ASC") ? article.publishDate.asc() : article.publishDate.desc();
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
            List<String> sourceIn,
            LocalDateTime publishedDateFrom,
            LocalDateTime publishedDateTo
    ) {
        QArticle article = QArticle.article;
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.isBlank()) {
            builder.and(article.title.containsIgnoreCase(keyword)
                    .or(article.summary.containsIgnoreCase(keyword)));
        }
        if (interestId != null) builder.and(article.interest.id.eq(interestId));
        if (sourceIn != null && !sourceIn.isEmpty()) builder.and(article.source.in(sourceIn));
        if (publishedDateFrom != null && publishedDateTo != null) {
            builder.and(article.publishDate.between(publishedDateFrom, publishedDateTo));
        }

        Long total = jpaQueryFactory
                .select(article.count())
                .from(article)
                .where(builder)
                .fetchOne();

        return total != null ? total : 0L;
    }
}
