package com.sprint.team2.monew.domain.comment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentActivityDto(
        UUID id,
        UUID articleId,
        String articleTitle,
        UUID userId,
        String userNickname,
        String content,
        long likeCount,
        LocalDateTime createdAt
) {
}
