package com.sprint.team2.monew.domain.article.exception;

import java.util.UUID;

public class ArticleNotFoundException extends ArticleException {
    public ArticleNotFoundException() {
        super(ArticleErrorCode.ARTICLE_NOT_FOUND);
    }

    public static ArticleNotFoundException withId(UUID articleId) {
        ArticleNotFoundException exception = new ArticleNotFoundException();
        exception.addDetail("articleId", articleId);
        return exception;
    }
}
