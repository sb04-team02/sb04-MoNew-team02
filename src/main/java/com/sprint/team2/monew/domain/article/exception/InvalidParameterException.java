package com.sprint.team2.monew.domain.article.exception;

public class InvalidParameterException extends ArticleException {
    public InvalidParameterException() {
        super(ArticleErrorCode.INVALIED_PARAMETER);
    }

    public static InvalidParameterException invalidParameter() {
        InvalidParameterException exception = new InvalidParameterException();
        return exception;
    }
}
