package com.sprint.team2.monew.domain.comment.dto.response;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseCommentDto(
        List<CommentDto> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
}
