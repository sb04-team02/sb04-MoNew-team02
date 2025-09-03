package com.sprint.team2.monew.domain.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseNotificationDto(
        List<NotificationDto> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
}
