package com.sprint.team2.monew.domain.comment.exception;

public class InvalidPageSizeException extends CommentException {
    public InvalidPageSizeException() {
        super(CommentErrorCode.INVALID_PAGE_SIZE);
    }

  public static InvalidPageSizeException of(int size) {
    InvalidPageSizeException exception = new InvalidPageSizeException();
    exception.addDetail("pageSize", size);
    return exception;
  }
}
