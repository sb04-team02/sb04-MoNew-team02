package com.sprint.team2.monew.domain.userActivity.events.commentEvent;

import java.util.UUID;

public record CommentLikeCancelEvent (
    UUID commentId,
    UUID commentUserId, //author
    long commentLikeCount
) {
}
