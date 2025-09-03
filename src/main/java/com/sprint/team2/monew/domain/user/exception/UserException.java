package com.sprint.team2.monew.domain.user.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class UserException extends BusinessException {
  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }
}
