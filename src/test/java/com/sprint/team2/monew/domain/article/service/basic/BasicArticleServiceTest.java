package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleMapper articleMapper;

    @InjectMocks
    private BasicArticleService basicArticleService;

    @Test
    @DisplayName("뉴스 기사 정렬: 정상적으로 정렬된 DTO 리시트 반환")
    void readArticleSortSuccess() {
        // given
        UUID userId = UUID.randomUUID();
        Article article = new Article();
        ArticleDto articleDto = new ArticleDto(
                UUID.randomUUID(),
                "source",
                "sourceUrl",
                "title",
                LocalDateTime.now().minusDays(1),
                "summary",
                0,
                0,
                false
        );

        when(articleRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(article)));

        when(articleMapper.toArticleDto(article))
                .thenReturn(articleDto);

        // when
        List<ArticleDto> result = basicArticleService.read(userId, "publishDate", "ASC", 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(articleDto);

        verify(articleRepository, times(1)).findAll(any(Pageable.class));
        verify(articleMapper, times(1)).toArticleDto(article);
    }
}