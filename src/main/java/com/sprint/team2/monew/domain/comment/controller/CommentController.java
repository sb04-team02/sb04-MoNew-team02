package com.sprint.team2.monew.domain.comment.controller;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     * POST /api/comments
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestBody @Valid CommentRegisterRequest request
    ) {
        log.info("댓글 작성 요청: articleId={}, userId={}", request.articleId(), request.userId());
        CommentDto createdComment = commentService.registerComment(request);
        log.info("댓글 작성 성공: commentId={}, articleId={}, userId={}",
                createdComment.id(), createdComment.articleId(), createdComment.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    /**
     * 댓글 수정
     * PATCH /api/comments/{commentId}
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @PathVariable UUID commentId,
            @RequestHeader(name = "Monew-Request-User-ID") UUID requesterUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        log.info("댓글 수정 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);
        CommentDto dto = commentService.updateComment(commentId, requesterUserId, request);
        log.info("댓글 수정 성공: commentId={}", dto.id());
        return ResponseEntity.ok(dto);
    }
}
