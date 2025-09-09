package com.sprint.team2.monew.domain.comment.service;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;

import java.util.UUID;

public interface CommentService {

    CommentDto registerComment(CommentRegisterRequest request);
    CommentDto updateComment(UUID commentId, UUID requesterUserId, CommentUpdateRequest request);
    void softDeleteComment(UUID commentId, UUID requesterUserId);
    void hardDeleteComment(UUID commentId, UUID requesterUserId);
}
