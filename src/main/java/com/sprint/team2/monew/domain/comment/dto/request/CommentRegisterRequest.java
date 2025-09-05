package com.sprint.team2.monew.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRegisterRequest(
        @NotNull(message = "articleId는 필수입니다")
        UUID articleId,
        @NotNull(message = "userId는 필수입니다")
        UUID userId,
        @NotBlank(message = "댓글 내용을 입력해주세요")
        String content
) {
}
