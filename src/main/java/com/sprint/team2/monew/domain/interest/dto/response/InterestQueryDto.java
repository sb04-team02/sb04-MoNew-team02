package com.sprint.team2.monew.domain.interest.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InterestQueryDto(
        UUID id,
        String name,
        List<String> keywords,
        long subscriberCount,
        boolean subscribedByMe,
        LocalDateTime createdAt
) {
}
