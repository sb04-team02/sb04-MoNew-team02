package com.sprint.team2.monew.domain.article.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
