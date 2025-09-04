package com.sprint.team2.monew.domain.notification.dto;

import com.sprint.team2.monew.domain.notification.entity.ResourceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID notificationId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    ResourceType resourceType,
    UUID resourceId
) { }
