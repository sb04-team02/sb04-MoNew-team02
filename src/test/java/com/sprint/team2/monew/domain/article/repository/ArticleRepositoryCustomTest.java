package com.sprint.team2.monew.domain.article.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import({QuerydslConfig.class, ArticleRepositoryCustom.class})
@ActiveProfiles("test")
@EnableJpaAuditing
class ArticleRepositoryCustomTest {
    @Autowired
    private ArticleRepositoryCustom articleRepositoryCustom;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("논리 삭제된 뉴스 기사는 조회 X")
    void searchArticlesExcludeDeleted() {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 9, 10, 12, 0);

        Article article1 = Article.builder()
                .title("Article 1 title")
                .summary("Article 1 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article1.com")
                .publishDate(LocalDateTime.now().minusDays(1))
                .commentCount(5)
                .viewCount(10)
                .build();

        Article article2 = Article.builder()
                .title("Article 2 title")
                .summary("Article 2 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article2.com")
                .publishDate(LocalDateTime.now())
                .commentCount(50)
                .viewCount(100)
                .build();

        articleRepository.save(article1);
        articleRepository.save(article2);

        Article deleted = Article.builder()
                .title("Deleted News title")
                .summary("Deleted News summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article3.com")
                .publishDate(LocalDateTime.now())
                .commentCount(1)
                .viewCount(1)
                .build();

        deleted.setDeletedAt(now.plusHours(1));
        articleRepository.save(deleted);

        // when
        List<Article> results = articleRepositoryCustom.searchArticles(
                null, null, null, null, null,
                ArticleOrderBy.publishDate, ArticleDirection.DESC,
                null, null, 10
        );

        // then
        assertThat(results)
                .extracting(Article::getTitle)
                .doesNotContain("Deleted News title");
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(Article::getTitle)
                .contains("Article 1 title", "Article 2 title");
    }

    @Test
    @DisplayName("키워드로 제목/요약 검색 가능")
    void searchArticlesWithKeyword() {
        // given
        Article article1 = Article.builder()
                .title("Article 1 title with keyword")
                .summary("Article 1 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article1.com")
                .publishDate(LocalDateTime.now().minusDays(1))
                .commentCount(5)
                .viewCount(10)
                .build();

        Article article2 = Article.builder()
                .title("Article 2 title")
                .summary("Article 2 summary")
                .source(ArticleSource.NAVER)
                .sourceUrl("https://article2.com")
                .publishDate(LocalDateTime.now())
                .commentCount(50)
                .viewCount(100)
                .build();

        articleRepository.save(article1);
        articleRepository.save(article2);

        // when
        List<Article> results = articleRepositoryCustom.searchArticles(
                "keyword", null, null, null, null,
                ArticleOrderBy.publishDate, ArticleDirection.DESC,
                null, null, 10
        );

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).contains("keyword");
    }
}