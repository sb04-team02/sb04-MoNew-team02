package com.sprint.team2.monew.domain.article.exception;

import java.util.UUID;

public class S3FailureException extends ArticleException {
  public S3FailureException() {
    super(ArticleErrorCode.S3_ACCESS_FAILED);
  }

  public static S3FailureException withId(UUID articleId) {
    S3FailureException exception = new S3FailureException();
    exception.addDetail("articleId", articleId);
    return exception;
  }}
