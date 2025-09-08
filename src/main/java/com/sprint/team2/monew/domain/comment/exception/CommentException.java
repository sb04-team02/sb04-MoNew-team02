package com.sprint.team2.monew.domain.comment.exception;

import com.sprint.team2.monew.global.constant.ErrorCode;
import com.sprint.team2.monew.global.error.BusinessException;

import java.util.Map;

public class CommentException extends BusinessException {
    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }
    public CommentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
