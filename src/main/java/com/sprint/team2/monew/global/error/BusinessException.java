package com.sprint.team2.monew.global.error;

import com.team1.hrbank.global.constant.ErrorCode;

public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;

  public BusinessException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public int getStatus() {
    return errorCode.getStatus();
  }

  public String getMessage() {
    return errorCode.getMessage();
  }

  public String getDetails() {
    return errorCode.getDetails();
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
