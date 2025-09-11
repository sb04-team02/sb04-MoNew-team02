package com.sprint.team2.monew.domain.article.exception;

public class ArticleSaveFailedException extends ArticleException {
    public ArticleSaveFailedException() {
        super(ArticleErrorCode.ARTICLE_SAVE_FAILED);
    }

    public static ArticleSaveFailedException articleSaveFailed() {
        ArticleSaveFailedException exception = new ArticleSaveFailedException();
        return exception;
    }
}
