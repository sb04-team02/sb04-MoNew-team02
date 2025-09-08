package com.sprint.team2.monew.domain.article.exception;

public class NaverApiEmptyResponseException extends ArticleException {
    public NaverApiEmptyResponseException() {
        super(ArticleErrorCode.EMPTY_RESPONSE);
    }
}
