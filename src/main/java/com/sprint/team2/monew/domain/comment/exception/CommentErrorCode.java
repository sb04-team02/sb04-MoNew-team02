package com.sprint.team2.monew.domain.comment.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "본인의 댓글만 수정할 수 있습니다."),
    COMMENT_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST.value(), "댓글 내용을 입력해주세요."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST.value(), "size는 1 이상이어야 합니다");

    private final int status;
    private final String message;

    @Override
    public String getErrorCodeName() {
        return name();
    }
}
