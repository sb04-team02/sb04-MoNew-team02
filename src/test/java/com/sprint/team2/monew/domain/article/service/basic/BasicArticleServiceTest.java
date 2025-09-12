package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.repository.ArticleRepositoryCustom;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepositoryCustom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicArticleServiceTest {
    @Mock
    private ArticleRepositoryCustom articleRepositoryCustom;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private UserActivityRepository userActivityRepository;
    @Mock
    private UserActivityRepositoryCustom userActivityRepositoryCustom;
    @Mock
    private UserActivity userActivity;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private BasicArticleService basicArticleService;

    @Test
    @DisplayName("뉴스 기사 정렬: 정상적으로 정렬된 DTO 리스트 반환 및 페이징 정보 반환")
    void readArticleSortSuccess() {
        // given
        UUID userId = UUID.randomUUID();

        Article article = new Article();
        article.setPublishDate(LocalDateTime.now().minusDays(1));

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

        when(articleRepositoryCustom.searchArticles(
                any(), any(), any(), any(), any(),
                eq(ArticleOrderBy.publishDate), eq(ArticleDirection.ASC),
                any(), any(), anyInt())
        ).thenReturn(List.of(article));

        when(articleMapper.toArticleDto(article)).thenReturn(articleDto);
        when(articleRepositoryCustom.countArticles(
                any(), any(), any(), any(), any()
        )).thenReturn(1L);

        // when
        CursorPageResponseArticleDto result = basicArticleService.read(
                userId, ArticleOrderBy.publishDate, ArticleDirection.ASC, 10,
                null,
                null, null, null, null,
                null, null);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0)).isEqualTo(articleDto);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isEqualTo(1L);
        verify(articleRepositoryCustom, times(1)).searchArticles(
                any(), any(), any(), any(), any(),
                eq(ArticleOrderBy.publishDate), eq(ArticleDirection.ASC),
                any(), any(), anyInt()
        );
        verify(articleMapper, times(1)).toArticleDto(article);
    }

    @Test
    @DisplayName("뉴스 기사 뷰 등록 성공: 첫 조회명 view count 증가")
    void viewArticleFirstTimeSuccess() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Article article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article.com")
                .title("title")
                .publishDate(LocalDateTime.now().minusDays(1))
                .summary("summary")
                .commentCount(0)
                .viewCount(0)
                .build();

        User user = User.builder()
                .email("user@monew.com")
                .nickname("user")
                .build();

        UserActivity userActivity = new UserActivity(userId, user.getEmail(), user.getNickname());
        userActivity.setId(UUID.randomUUID());

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userActivityRepository.findById(userId)).thenReturn(Optional.of(userActivity));
        when(userActivityRepository.save(any(UserActivity.class))).thenReturn(userActivity);
        when(commentRepository.countByArticleId(articleId)).thenReturn(0L);

        // when
        ArticleViewDto result = basicArticleService.view(userId, articleId);

        // then
        assertThat(result.viewedBy()).isEqualTo(userId);
        assertThat(result.articleViewCount()).isEqualTo(1);
        assertThat(result.articleCommentCount()).isEqualTo(0);

        verify(articleRepository, times(1)).save(any(Article.class));
        verify(userActivityRepository, times(1)).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("논리 삭제 성공: deletedAt이 현재 시간이 됨")
    void softDeleteSuccess() {
        UUID articleId = UUID.randomUUID();
        Article article = Article.builder()
                .title("Article 1 title")
                .summary("Article 1 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article1.com")
                .publishDate(LocalDateTime.now().minusDays(1))
                .commentCount(5)
                .viewCount(10)
                .build();

        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // when
        basicArticleService.softDelete(articleId);

        // then
        assertThat(article.getDeletedAt()).isNotNull();
        verify(articleRepository, times(1)).findById(articleId);
    }

    @Test
    @DisplayName("물리 삭제 성공")
    void hardDeleteSuccess() {
        // given
        UUID articleId = UUID.randomUUID();
        Article article = Article.builder()
                .title("Article 1 title")
                .summary("Article 1 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article1.com")
                .publishDate(LocalDateTime.now().minusDays(1))
                .commentCount(5)
                .viewCount(10)
                .build();

        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        // when
        basicArticleService.hardDelete(articleId);

        // then
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).delete(article);
        verify(userActivityRepositoryCustom, times(1)).deleteByArticleId(articleId);
    }
}