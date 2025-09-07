package com.sprint.team2.monew.domain.comment.repository;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Comment c set c.likeCount = c.likeCount + 1 where c.id = :id")
    int incrementLikeCount(@Param("id") UUID commentId);
}
