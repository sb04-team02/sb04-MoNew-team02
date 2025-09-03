package com.sprint.team2.monew.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String email,
        String nickname,
        LocalDateTime createdAt
) {
}
