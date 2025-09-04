package com.sprint.team2.monew.domain.like.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeDto(
        UUID id,
        UUID likedBy, //좋아요한 사용자 Id
        LocalDateTime createdAt,
        UUID commentId,
        UUID articleId,
        UUID commentUserId,
        String commentUserNickname,
        String commentContent,
        long commentLikeCount,
        LocalDateTime commentCreatedAt
) {
}
