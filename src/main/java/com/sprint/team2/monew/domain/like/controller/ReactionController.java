package com.sprint.team2.monew.domain.like.controller;

import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/{commentId}/comment-likes")
public class ReactionController {

    private final ReactionService reactionService;

    /**
     * 댓글 좋아요 생성
     * POST /api/comments/{commentId}/comment-likes
     */
    @PostMapping
    public ResponseEntity<CommentLikeDto> like(@PathVariable UUID commentId,
                                               @RequestHeader("Monew-Request-User-ID") UUID requesterUserId) {
        log.info("댓글 좋아요 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);
        CommentLikeDto commentLikeDto = reactionService.likeComment(commentId, requesterUserId);
        log.info("댓글 좋아요 성공: reactionId={}, commentId={}, likedBy={}", commentLikeDto.id(), commentLikeDto.commentId(), commentLikeDto.likedBy());
        return ResponseEntity.status(HttpStatus.OK).body(commentLikeDto);
    }
}
