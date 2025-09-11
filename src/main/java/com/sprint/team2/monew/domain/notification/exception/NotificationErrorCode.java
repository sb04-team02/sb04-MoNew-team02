package com.sprint.team2.monew.domain.notification.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Notification not found"),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST.value(), "Invalid format");

    private int status;
    private String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
