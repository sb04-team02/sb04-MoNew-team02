package com.sprint.team2.monew.domain.like.service;

import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;

import java.util.UUID;

public interface ReactionService {
    CommentLikeDto likeComment(UUID commentId, UUID requesterUserId);

}
