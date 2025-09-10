package com.sprint.team2.monew.domain.notification.exception;

import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {
    public NotificationNotFoundException() { super(NotificationErrorCode.NOTIFICATION_NOT_FOUND);}

    public static NotificationNotFoundException withId(UUID notificationId) {
        NotificationNotFoundException exception = new NotificationNotFoundException();
        exception.addDetail("notificationId",notificationId);
        return exception;
    }
}
