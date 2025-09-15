package com.sprint.team2.monew.domain.comment.service;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.sprint.team2.monew.domain.comment.entity.CommentSortType;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface CommentService {

    CommentDto registerComment(CommentRegisterRequest request);
    CommentDto updateComment(UUID commentId, UUID requesterUserId, CommentUpdateRequest request);
    void softDeleteComment(UUID commentId, UUID requesterUserId);
    void hardDeleteComment(UUID commentId, UUID requesterUserId);
    CursorPageResponseCommentDto getAllArticleComment(UUID articleId, UUID requesterUserId,
                                                      String cursor, int size,
                                                      OffsetDateTime after,
                                                      CommentSortType sortType, boolean asc);
}
