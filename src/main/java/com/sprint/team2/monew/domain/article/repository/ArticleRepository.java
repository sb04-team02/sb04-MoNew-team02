package com.sprint.team2.monew.domain.article.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsBySourceUrlAndDeletedAtIsNull(String sourceUrl);

    @Query("SELECT a.sourceUrl FROM Article a"
        + " WHERE a.createdAt >= :startDate AND a.createdAt < :endDate ")
    Set<String> findArticleUrlsBetweenDates(
        @Param("startDate") LocalDateTime start,
        @Param("endDate") LocalDateTime end);
}

