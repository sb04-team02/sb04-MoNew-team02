package com.sprint.team2.monew.domain.userActivity.exception;
import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class UserActivityException extends BusinessException {
  public UserActivityException(ErrorCode errorCode) {
    super(errorCode);
  }
}

