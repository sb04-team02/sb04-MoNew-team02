package com.sprint.team2.monew.domain.subscription.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class SubscriptionException extends BusinessException {
  public SubscriptionException(ErrorCode errorCode) {
    super(errorCode);
  }
}
