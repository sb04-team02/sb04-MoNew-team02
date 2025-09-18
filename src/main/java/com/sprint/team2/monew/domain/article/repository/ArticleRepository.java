package com.sprint.team2.monew.domain.article.repository;

import com.sprint.team2.monew.domain.article.entity.Article;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    boolean existsBySourceUrlAndDeletedAtIsNull(String sourceUrl);

    List<Article> findByPublishDateBetween(LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("UPDATE Article a SET a.commentCount = a.commentCount + 1 WHERE a.id = :articleId")
    void increaseCommentCount(@Param("articleId") UUID articleId);

    @Modifying
    @Query("UPDATE Article a SET a.commentCount = a.commentCount - 1 WHERE a.id = :articleId AND a.commentCount > 0")
    void decreaseCommentCount(@Param("articleId") UUID articleId);
}

