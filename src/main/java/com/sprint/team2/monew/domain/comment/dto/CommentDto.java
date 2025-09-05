package com.sprint.team2.monew.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID articleId,
        UUID userId,
        String userNickname,
        String content,
        long likeCount, //좋아요 수
        boolean likedByMe, //요청자의 좋아요 여부
        LocalDateTime createdAt
) {
}
