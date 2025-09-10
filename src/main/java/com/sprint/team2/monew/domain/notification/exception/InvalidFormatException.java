package com.sprint.team2.monew.domain.notification.exception;

public class InvalidFormatException extends NotificationException {
    public InvalidFormatException() {super(NotificationErrorCode.INVALID_FORMAT);}

    public static InvalidFormatException withField(String field) {
        return new InvalidFormatException();
    }
}
