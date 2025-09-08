package com.sprint.team2.monew.domain.notification.service;

import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.event.CommentLikedEvent;
import com.sprint.team2.monew.domain.notification.event.InterestArticleRegisteredEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationService {

    void notifyInterestArticleRegistered(InterestArticleRegisteredEvent event);

    void notifyCommentLiked(CommentLikedEvent event);

    void confirmNotification(UUID notificationId);

    void confirmAllNotifications(UUID userId);

    CursorPageResponseNotificationDto getAllNotifications(UUID userId, LocalDateTime nextAfter, int size);

    void deleteConfirmedNotifications();
}
