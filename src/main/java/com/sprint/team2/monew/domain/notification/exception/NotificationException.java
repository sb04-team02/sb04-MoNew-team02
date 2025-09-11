package com.sprint.team2.monew.domain.notification.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

public class NotificationException extends BusinessException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
