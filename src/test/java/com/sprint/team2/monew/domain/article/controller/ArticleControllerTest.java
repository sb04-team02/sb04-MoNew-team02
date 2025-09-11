package com.sprint.team2.monew.domain.article.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.exception.ArticleNotFoundException;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleService articleService;

    @Test
    @DisplayName("뉴스 기사 논리 삭제 성공")
    void softDeleteSuccess() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();

        doNothing().when(articleService).softDelete(articleId);

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}", articleId))
                .andExpect(status().isNoContent());

        verify(articleService, times(1)).softDelete(articleId);
    }

    @Test
    @DisplayName("뉴스 기사 논리 삭제 실패: 존재하지 않는 id")
    void softDeleteFailWhenNotFound() throws Exception {
        // given
        UUID invalidId = UUID.randomUUID();

        doThrow(ArticleNotFoundException.withId(invalidId)).when(articleService).softDelete(invalidId);

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}", invalidId))
                .andExpect(status().isNotFound());

        verify(articleService, times(1)).softDelete(invalidId);
    }

    @Test
    @DisplayName("뉴스 기사 물리 삭제 성공")
    void hardDeleteSuccess() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();
        doNothing().when(articleService).hardDelete(articleId);

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}/hard", articleId))
                .andExpect(status().isNoContent());

        verify(articleService, times(1)).hardDelete(articleId);
    }

    @Test
    @DisplayName("뉴스 기사 물리 삭제 실패 존재하지 않은 id")
    void hardDeleteFailWhenNotFound() throws Exception {
        // given
        UUID invalidId = UUID.randomUUID();

        doThrow(ArticleNotFoundException.withId(invalidId)).when(articleService).hardDelete(invalidId);

        // when & then
        mockMvc.perform(delete("/api/articles/{articleId}/hard", invalidId))
                .andExpect(status().isNotFound());

        verify(articleService, times(1)).hardDelete(invalidId);
    }
}