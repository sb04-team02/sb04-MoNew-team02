package com.sprint.team2.monew.domain.comment.controller;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.sprint.team2.monew.domain.comment.entity.CommentSortType;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    /**
     * 댓글 논리 삭제
     * DELETE /api/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable UUID commentId,
            @RequestHeader(name = "Monew-Request-User-ID") UUID requesterUserId) {
        log.info("댓글 논리 삭제 요청 수신: commentId={}, requesterUserId={}", commentId, requesterUserId);
        commentService.softDeleteComment(commentId, requesterUserId);

        log.info("댓글 논리 삭제 완료: commentId={}, requesterUserId={}", commentId, requesterUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 물리 삭제
     * DELETE /api/comments/{commentId}/hard
     */
    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDeleteComment(
            @PathVariable UUID commentId,
            @RequestHeader(name = "Monew-Request-User-ID") UUID requesterUserId) {
        log.info("댓글 물리 삭제 요청 수신: commentId={}, requesterUserId={}", commentId, requesterUserId);
        commentService.hardDeleteComment(commentId, requesterUserId);

        log.info("댓글 물리 삭제 완료: commentId={}, requesterUserId={}", commentId, requesterUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 목록 조회
     * GET /api/comments
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseCommentDto> getComments(
            @RequestParam("articleId") UUID articleId,
            @RequestParam("orderBy") String orderBy,
            @RequestParam("direction") String direction,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "after", required = false) LocalDateTime after,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestHeader("Monew-Request-User-ID") UUID requesterUserId
    ) {
        // orderBy 매핑 → 서비스 enum
        CommentSortType sortType = "likeCount".equalsIgnoreCase(orderBy)
                ? CommentSortType.LIKE_COUNT
                : CommentSortType.DATE;

        // direction 매핑 → asc boolean
        boolean asc = !"DESC".equalsIgnoreCase(direction); // 기본 ASC

        CursorPageResponseCommentDto body = commentService.getAllArticleComment(
                articleId,
                requesterUserId,
                cursor,
                limit,
                sortType,
                asc
        );

        return ResponseEntity.ok(body);
    }
}
