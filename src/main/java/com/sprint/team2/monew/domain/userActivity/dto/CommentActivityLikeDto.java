package com.sprint.team2.monew.domain.userActivity.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentActivityLikeDto (
    UUID id,
    LocalDateTime createdAt,
    UUID commentId,
    UUID articleId,
    String articleTitle,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    long commentLikeCount,
    LocalDateTime commentCreatedAt
) {

}
