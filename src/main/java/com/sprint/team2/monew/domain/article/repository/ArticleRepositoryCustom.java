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

        if (cursor != null && after != null) {
            switch (orderBy) {
                case "commentCount" -> builder.and(
                        direction.equalsIgnoreCase("ASC") ?
                                article.commentCount.gt(Integer.parseInt(cursor)) :
                                article.commentCount.lt(Integer.parseInt(cursor))
                );
                case "viewCount" -> builder.and(
                        direction.equalsIgnoreCase("ASC") ?
                                article.viewCount.gt(Integer.parseInt(cursor)) :
                                article.viewCount.lt(Integer.parseInt(cursor))
                );
                default -> { // publishDate 기준
                    LocalDateTime cursorDate = LocalDateTime.parse(cursor);
                    builder.and(
                            direction.equalsIgnoreCase("ASC") ?
                                    article.publishDate.gt(cursorDate) :
                                    article.publishDate.lt(cursorDate)
                    );
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
                .orderBy(orderSpecifier)
                .limit(limit + 1)
                .fetch();
    }
}
