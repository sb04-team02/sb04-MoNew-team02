package com.sprint.team2.monew.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.exception.CommentContentRequiredException;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    // ===== registerComment =====

    @Test
    @DisplayName("댓글 작성 성공 - 201")
    void createCommentSuccess() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentRegisterRequest req = new CommentRegisterRequest(
                articleId, userId, "새 댓글 내용"
        );

        CommentDto res = new CommentDto(
                UUID.randomUUID(),
                articleId,
                userId,
                "닉",
                "새 댓글 내용",
                0L,
                false,
                LocalDateTime.now()
        );

        given(commentService.registerComment(any(CommentRegisterRequest.class))).willReturn(res);

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(res.id().toString()))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.content").value("새 댓글 내용"));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 검증 오류 400")
    void createCommentFailValidationBadRequest() throws Exception {
        // given: content가 @NotBlank 위반
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest req = new CommentRegisterRequest(articleId, userId, "");

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest());

        // 서비스는 호출되지 않아야 함
        verifyNoInteractions(commentService);
    }

    // ===== updateComment =====
    
    @Test
    @DisplayName("댓글 수정 성공 - 200")
    void updateCommentSuccess() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        CommentUpdateRequest req = new CommentUpdateRequest("수정된 내용");

        CommentDto res = new CommentDto(
                commentId,
                UUID.randomUUID(),   // articleId는 단순히 값만 채움
                requesterId,
                "닉",
                "수정된 내용",
                0L,
                false,
                LocalDateTime.now()
        );

        given(commentService.updateComment(eq(commentId), eq(requesterId), any(CommentUpdateRequest.class)))
                .willReturn(res);

        // when & then
        mockMvc.perform(
                        patch("/api/comments/{commentId}", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Monew-Request-User-ID", requesterId.toString())
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 잘못된 입력(빈 내용)-400")
    void updateCommentFailValidationBadRequest() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        CommentUpdateRequest req = new CommentUpdateRequest("    ");

        // 서비스에서 도메인 예외(400) 시뮬레이션
        given(commentService.updateComment(eq(commentId), eq(requesterId), any(CommentUpdateRequest.class)))
                .willThrow(CommentContentRequiredException.commentContentRequiredForUpdate(commentId));

        // when & then
        mockMvc.perform(
                        patch("/api/comments/{commentId}", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Monew-Request-User-ID", requesterId.toString())
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("댓글 내용을 입력해주세요")))
                .andExpect(jsonPath("$.details.commentId").value(commentId.toString()));
    }

    // ===== SoftDeleteComment =====

    @Test
    @DisplayName("댓글 논리삭제 성공-204")
    void softDeleteCommentSuccess() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willDoNothing().given(commentService).softDeleteComment(commentId, requesterUserId);

        //when + then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isNoContent());

        then(commentService).should().softDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();

    }

    @Test
    @DisplayName("댓글 논리삭제 실패-404")
    void softDeleteCommentFailWhenCommentNotFound() throws Exception {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willThrow(new ContentNotFoundException()).given(commentService)
                .softDeleteComment(commentId, requesterUserId);

        //when + then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isNotFound());

        then(commentService).should().softDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 논리삭제 실패-500")
    void softDeleteCommentFailWhenInternalServerError() throws Exception{
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willThrow(new RuntimeException("unexpected"))
                .given(commentService).softDeleteComment(commentId, requesterUserId);
        //when + then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isInternalServerError());

        then(commentService).should().softDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();
    }

    // ===== HardDeleteComment =====
    
    @Test
    @DisplayName("댓글 물리삭제 성공-204 No Content")
    void hardDeleteCommentSuccess() throws Exception{
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willDoNothing().given(commentService).hardDeleteComment(commentId, requesterUserId);
        
        //when +then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isNoContent());

        then(commentService).should().hardDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 댓글 정보 없음-404 Not Found")
    void hardDeleteCommentNotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willThrow(new ContentNotFoundException())
                .given(commentService).hardDeleteComment(commentId, requesterUserId);

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isNotFound());

        then(commentService).should().hardDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 물리 삭제 실패 서버 내부 오류-500 Internal Server Error")
    void hardDeleteCommentISE() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();
        willThrow(new RuntimeException("unexpected"))
                .given(commentService).hardDeleteComment(commentId, requesterUserId);

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Monew-Request-User-ID", requesterUserId.toString()))
                .andExpect(status().isInternalServerError());

        then(commentService).should().hardDeleteComment(commentId, requesterUserId);
        then(commentService).shouldHaveNoMoreInteractions();
    }
}
