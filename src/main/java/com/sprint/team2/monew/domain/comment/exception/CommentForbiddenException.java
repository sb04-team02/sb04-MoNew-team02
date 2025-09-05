package com.sprint.team2.monew.domain.comment.exception;

import java.util.Map;
import java.util.UUID;

public class CommentForbiddenException extends CommentException {
  public CommentForbiddenException() {
    super(CommentErrorCode.COMMENT_FORBIDDEN);
  }

  public static CommentForbiddenException commentForbiddenException(UUID commentId, UUID ownerId, UUID articleId) {
    CommentForbiddenException exception = new CommentForbiddenException();
    exception.addDetail("commentId", commentId);
    exception.addDetail("ownerId", ownerId);
    exception.addDetail("articleId", articleId);
    return exception;
  }
}
