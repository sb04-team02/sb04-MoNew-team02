package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.user.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.sprint.team2.monew.domain.notification.entity.QNotification.notification;

public class TestNotificationFactory {
    public static Notification createNotification(ResourceType resourceType, UUID resourceId, String content) {
        User user = TestUserFactory.createUser();
        Notification notification = Notification.builder()
                .user(user)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .confirmed(false)
                .content(content)
                .build();

        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());
        return notification;
    }
}
