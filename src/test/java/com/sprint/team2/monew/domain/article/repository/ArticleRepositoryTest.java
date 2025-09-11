package com.sprint.team2.monew.domain.article.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@EnableJpaAuditing
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("삭제되지 않은 동일한 URL을 가진 기사가 있으면 true 반환")
    void existsBySourceUrlAndDeletedAtIsNullSuccess() {
        // given
        String url = "https://article.com/article1";
        Article article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl(url)
                .title("title")
                .publishDate(LocalDateTime.now())
                .summary("summary")
                .build();

        articleRepository.save(article);

        // when
        boolean exists = articleRepository.existsBySourceUrlAndDeletedAtIsNull(url);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 URL에 false 반환")
    void existsBySourceUrlAndDeletedAtIsNullNoExists() {
        // when
        boolean exists = articleRepository.existsBySourceUrlAndDeletedAtIsNull("https://article-no-exist.com");

        // then
        assertThat(exists).isFalse();
    }
}