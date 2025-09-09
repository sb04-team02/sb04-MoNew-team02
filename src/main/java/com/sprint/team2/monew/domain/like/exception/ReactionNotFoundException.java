package com.sprint.team2.monew.domain.like.exception;

import com.sprint.team2.monew.global.error.BusinessException;

import java.util.UUID;

public class ReactionNotFoundException extends BusinessException {
  public ReactionNotFoundException() {
    super(ReactionErrorCode.REACTION_NOT_FOUND);
  }

  public static ReactionNotFoundException reactionNotFound(UUID commentId, UUID userId) {
    ReactionNotFoundException e = new ReactionNotFoundException();
    e.addDetail("commentId", commentId);
    e.addDetail("userId", userId);
    return e;
  }

  public static ReactionNotFoundException forUnlike(UUID commentId, UUID userId) {
    return reactionNotFound(commentId, userId);
  }
}
