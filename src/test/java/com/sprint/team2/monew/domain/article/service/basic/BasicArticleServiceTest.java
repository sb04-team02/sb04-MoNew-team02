package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.collect.Collector;
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
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepositoryCustom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private UserActivityMapper userActivityMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private Collector rssCollector;

    private BasicArticleService basicArticleService;

    @BeforeEach
    void setUp() {
        basicArticleService = new BasicArticleService(
                articleMapper,
                articleRepository,
                articleRepositoryCustom,
                List.of(rssCollector),
                null,
                publisher,
                userActivityRepository,
                userActivityRepositoryCustom,
                commentRepository,
                userRepository,
                userActivityMapper
        );
    }

    @Test
    @DisplayName("뉴스 기사 정렬: 정상적으로 정렬된 DTO 리스트 반환 및 페이징 정보 반환")
    void readArticleSortSuccess() {
        UUID userId = UUID.randomUUID();
        LocalDateTime publishDate = LocalDateTime.now().minusDays(1);
        UUID articleId = UUID.randomUUID();

        Article article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article.com")
                .title("title")
                .publishDate(publishDate)
                .summary("summary")
                .commentCount(0)
                .viewCount(0)
                .build();
        ReflectionTestUtils.setField(article, "id", articleId);

        when(articleRepositoryCustom.searchArticles(
                any(), any(), any(), any(), any(),
                eq(ArticleOrderBy.publishDate), eq(ArticleDirection.ASC),
                any(), any(), anyInt())
        ).thenReturn(List.of(article));

        when(articleRepositoryCustom.countArticles(
                any(), any(), any(), any(), any())
        ).thenReturn(1L);

        CursorPageResponseArticleDto result = basicArticleService.read(
                userId, ArticleOrderBy.publishDate, ArticleDirection.ASC, 10,
                null, null, null, null, null,
                null, null
        );

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("title");
        assertThat(result.content().get(0).summary()).isEqualTo("summary");
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("뉴스 기사 뷰 등록 성공: 첫 조회명 view count 증가")
    void viewArticleFirstTimeSuccess() {
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

        // User는 protected 생성자여서 Mockito mock 사용
        com.sprint.team2.monew.domain.user.entity.User user = mock(com.sprint.team2.monew.domain.user.entity.User.class);

        UserActivity userActivity = new UserActivity(userId, "user@monew.com", "user");

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userActivityRepository.findById(userId)).thenReturn(Optional.of(userActivity));

        when(userActivityMapper.toArticleViewEvent(any(ArticleViewDto.class)))
                .thenAnswer(invocation -> {
                    ArticleViewDto dto = invocation.getArgument(0);
                    return new ArticleViewEvent(
                            dto.id(),
                            dto.viewedBy(),
                            dto.createdAt(),
                            dto.articleId(),
                            dto.source(),
                            dto.sourceUrl(),
                            dto.articleTitle(),
                            dto.articlePublishedDate(),
                            dto.articleSummary(),
                            dto.articleCommentCount(),
                            dto.articleViewCount()
                    );
                });

        ArticleViewDto result = basicArticleService.view(userId, articleId);

        assertThat(result.viewedBy()).isEqualTo(userId);
        assertThat(result.articleViewCount()).isEqualTo(1);

        verify(articleRepository, times(1)).save(article);
        verify(userActivityRepository, times(1)).findById(userId);
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

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        basicArticleService.softDelete(articleId);

        assertThat(article.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("물리 삭제 성공")
    void hardDeleteSuccess() {
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

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        basicArticleService.hardDelete(articleId);

        verify(articleRepository, times(1)).delete(article);
    }
}
