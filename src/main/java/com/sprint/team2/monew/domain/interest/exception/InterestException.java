package com.sprint.team2.monew.domain.interest.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class InterestException extends BusinessException {
  public InterestException(ErrorCode errorCode) {
    super(errorCode);
  }
}
