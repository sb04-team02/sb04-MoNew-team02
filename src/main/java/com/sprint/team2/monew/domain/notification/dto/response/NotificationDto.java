package com.sprint.team2.monew.domain.notification.dto.response;

import com.sprint.team2.monew.domain.notification.entity.ResourceType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    ResourceType resourceType,
    UUID resourceId
) { }
