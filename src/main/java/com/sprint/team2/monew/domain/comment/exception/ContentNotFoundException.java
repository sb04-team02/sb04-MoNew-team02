package com.sprint.team2.monew.domain.comment.exception;

import java.util.Map;
import java.util.UUID;

public class ContentNotFoundException extends CommentException {
    public ContentNotFoundException() {super(CommentErrorCode.COMMENT_NOT_FOUND);}

    public ContentNotFoundException(Map<String, Object> details) {
        super(CommentErrorCode.COMMENT_NOT_FOUND, details);
    }

    public static ContentNotFoundException contentNotFoundException(UUID commentId) {
        ContentNotFoundException exception = new ContentNotFoundException();
        exception.addDetail("commentId", commentId);
        return exception;
    }
}
