package com.sprint.team2.monew.domain.article.controller;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.article.exception.ArticleNotFoundException;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import com.sprint.team2.monew.domain.article.service.ArticleStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;
    @MockitoBean
    private ArticleStorageService articleStorageService;

    @Test
    @DisplayName("뉴스 기사 뷰 등록 성공")
    void postArticleViewSuccess() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ArticleViewDto articleViewDto = new ArticleViewDto(
                id,
                userId,
                LocalDateTime.now(),
                articleId,
                "source",
                "sourceUrl",
                "title",
                LocalDateTime.now().minusDays(1),
                "summary",
                5,
                10
        );

        when(articleService.view(userId, articleId)).thenReturn(articleViewDto);

        // when & then
        mockMvc.perform(post("/api/articles/{articleId}/article-views", articleId)
                        .header("Monew-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.articleCommentCount").value(5))
                .andExpect(jsonPath("$.articleViewCount").value(10))
        ;

        verify(articleService, times(1)).view(userId, articleId);
    }

    @Test
    @DisplayName("뉴스 기사 뷰 등록 실패: 존재하지 않는 뉴스 기사 id")
    void getArticleViewFailWhenArticleIdNotFound() throws Exception {
        // given
        UUID invalidId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(articleService.view(userId, invalidId))
                .thenThrow(ArticleNotFoundException.withId(invalidId));

        // when & then
        mockMvc.perform(post("/api/articles/{articleId}/article-views", invalidId)
                        .header("Monew-Request-User-ID", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(articleService, times(1)).view(userId, invalidId);
    }

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