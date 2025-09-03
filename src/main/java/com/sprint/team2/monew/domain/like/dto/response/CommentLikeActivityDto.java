package com.sprint.team2.monew.domain.like.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeActivityDto(
        UUID id, //좋아요 ID
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
