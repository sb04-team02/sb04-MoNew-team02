package com.sprint.team2.monew.domain.article.exception;

public class ArticleCollectFailedException extends ArticleException {
    public ArticleCollectFailedException() {
        super(ArticleErrorCode.ARTICLE_COLLECT_FAILED);
    }

    public static ArticleCollectFailedException withKeyword(String keyword) {
        ArticleCollectFailedException exception = new ArticleCollectFailedException();
        exception.addDetail("keyword", keyword);
        return exception;
    }
}
