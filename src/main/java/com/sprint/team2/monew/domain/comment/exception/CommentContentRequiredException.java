package com.sprint.team2.monew.domain.comment.exception;

import java.util.UUID;

public class CommentContentRequiredException extends CommentException {
    public CommentContentRequiredException() {
        super(CommentErrorCode.COMMENT_CONTENT_REQUIRED);
    }

    /** 등록 시 */
    public static CommentContentRequiredException commentContentRequiredForCreate(UUID articleId, UUID userId) {
        CommentContentRequiredException exception = new CommentContentRequiredException();
        exception.addDetail("articleId", articleId);
        exception.addDetail("userId", userId);
        return exception;
    }

    /** 수정 시 */
    public static CommentContentRequiredException commentContentRequiredForUpdate(UUID commentId) {
        CommentContentRequiredException exception = new CommentContentRequiredException();
        exception.addDetail("commentId", commentId);
        return exception;
    }
}
