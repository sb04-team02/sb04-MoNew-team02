package com.sprint.team2.monew.domain.like.exception;

import com.sprint.team2.monew.global.error.BusinessException;

import java.util.UUID;

public class ReactionAlreadyExistsException extends BusinessException {
    public ReactionAlreadyExistsException() { super(ReactionErrorCode.REACTION_ALREADY_EXISTS); }

    public static ReactionAlreadyExistsException reactionAlreadyExists(UUID commentId, UUID userId) {
        ReactionAlreadyExistsException e = new ReactionAlreadyExistsException();
        e.addDetail("commentId", commentId);
        e.addDetail("userId", userId);
        return e;
    }
}
