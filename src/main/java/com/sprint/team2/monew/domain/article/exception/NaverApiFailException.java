package com.sprint.team2.monew.domain.article.exception;

public class NaverApiFailException extends ArticleException {
    public NaverApiFailException(String message) {
        super(ArticleErrorCode.NAVER_API_FAIL);
    }
}
