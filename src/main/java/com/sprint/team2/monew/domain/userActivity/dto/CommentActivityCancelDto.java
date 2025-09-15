package com.sprint.team2.monew.domain.userActivity.dto;

import java.util.UUID;

public record CommentActivityCancelDto(
    UUID id,
    UUID commentUserId
) {

}
