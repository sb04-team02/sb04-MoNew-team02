package com.sprint.team2.monew.domain.like.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.service.ReactionService;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReactionController.class)
@Import(GlobalExceptionHandler.class)
public class ReactionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ReactionService reactionService;

    @Test
    @DisplayName("댓글 좋아요 생성 성공")
    void likeCommentSuccess() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        CommentLikeDto dto = new CommentLikeDto(
                UUID.randomUUID(), requesterId, LocalDateTime.now(),
                commentId, UUID.randomUUID(),
                UUID.randomUUID(), "작성자", "댓글 내용", 1L, LocalDateTime.now()
        );

        given(reactionService.likeComment(eq(commentId), eq(requesterId))).willReturn(dto);

        mockMvc.perform(
                        post("/api/comments/{commentId}/comment-likes", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Monew-Request-User-ID", requesterId.toString())
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dto.id().toString()))
                .andExpect(jsonPath("$.commentId").value(commentId.toString()))
                .andExpect(jsonPath("$.likedBy").value(requesterId.toString()))
                .andExpect(jsonPath("$.commentLikeCount").value(1));
    }

    @Test
    @DisplayName("댓글 좋아요 생성 실패 - 댓글이 존재하지 않음")
    void likeCommentFailWhenContentNotFound() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reactionService.likeComment(eq(commentId), eq(requesterId)))
                .willThrow(ContentNotFoundException.contentNotFoundException(commentId));

        mockMvc.perform(
                        post("/api/comments/{commentId}/comment-likes", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Monew-Request-User-ID", requesterId.toString())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공")
    void likeCommentCancelSuccess() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();

        willDoNothing().given(reactionService).unlikeComment(commentId, requesterUserId);

        //when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString())
        );

        //then
        resultActions.andExpect(status().isOk());
        then(reactionService).should().unlikeComment(commentId, requesterUserId);
        then(reactionService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 좋아요 취소 실패 - 댓글이 존재하지 않음")
    void likeCommentCancelFailCommentNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();

        willThrow(new ContentNotFoundException())
                .given(reactionService).unlikeComment(commentId, requesterUserId);

        //when + then
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString())
        ).andExpect(status().isNotFound());

        then(reactionService).should().unlikeComment(commentId, requesterUserId);
        then(reactionService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 좋아요 취소 실패 - 사용자가 존재하지 않음")
    void likeCommentCancelFailUserNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();

        willThrow(new UserNotFoundException())
                .given(reactionService).unlikeComment(commentId, requesterUserId);

        //when + then
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/comments/{commentId}/comment-likes", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString())
        ).andExpect(status().isNotFound());

        then(reactionService).should().unlikeComment(commentId, requesterUserId);
        then(reactionService).shouldHaveNoMoreInteractions();
    }
}
