package com.sprint.team2.monew.domain.userActivity.dto;

import java.util.UUID;

public record CommentActivityCancelDto(
    UUID commentId,
    UUID commentUserId, //author
    long commentLikeCount
) {

}
