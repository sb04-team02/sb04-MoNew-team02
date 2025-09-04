package com.sprint.team2.monew.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.service.CommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    @Test
    void 댓글_작성_성공_201과_응답바디() throws Exception {
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
    void 댓글_작성_실패_검증오류_400() throws Exception {
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

    @Test
    void 댓글_수정_성공_200과_응답바디() throws Exception {
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
    void 댓글_수정_실패_권한없음_403() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        CommentUpdateRequest req = new CommentUpdateRequest("수정 요청");

        // 서비스에서 권한 예외 발생 시뮬레이션
        given(commentService.updateComment(eq(commentId), eq(requesterId), any(CommentUpdateRequest.class)))
                .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다."));

        // when & then
        mockMvc.perform(
                        patch("/api/comments/{commentId}", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Monew-Request-User-ID", requesterId.toString())
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isForbidden());
    }
}
