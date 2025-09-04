package com.sprint.team2.monew.domain.comment.controller;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     * POST /comments
     */
    @PostMapping
    public ResponseEntity<CommentDto> createComment(
            @RequestBody @Valid CommentRegisterRequest request
    ) {
        CommentDto createdComment = commentService.registerComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @PathVariable UUID commentId,
            @RequestHeader(name = "Monew-Request-User-ID") UUID requesterUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        var dto = commentService.updateComment(commentId, requesterUserId, request);
        return ResponseEntity.ok(dto);
    }
}
