package com.sprint.team2.monew.domain.comment.repository;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query( value = """
        update comments
            set like_count = like_count + 1
        where id = :id
        returning like_count
    """, nativeQuery = true)
    long incrementLikeCountReturning(@Param("id") UUID commentId);

    @Modifying
    @Query("""
           update Comment c
              set c.likeCount = case when c.likeCount > 0 then c.likeCount - 1 else 0 end
            where c.id = :id
           """)
    int decrementLikeCount(@Param("id") UUID commentId);

    Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);

    // 날짜 기준 커서 페이지네이션
    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deletedAt is null
          and (:afterDate is null or
               (:isAsc = true  and c.createdAt > :afterDate) or
               (:isAsc = false and c.createdAt < :afterDate))
          and (:cursor is null or
               (:isAsc = true  and c.createdAt > :cursor) or
               (:isAsc = false and c.createdAt < :cursor))
""")
    Slice<Comment> findByArticle_IdWithDateCursor(
            @Param("articleId") UUID articleId,
            @Param("cursor") LocalDateTime cursor,
            @Param("afterDate") LocalDateTime afterDate,
            @Param("isAsc") boolean isAsc,
            Pageable pageable
    );

    // 좋아요 수 기준 커서 페이지네이션 (Slice 활용)
    @Query("""
        select c from Comment c
        where c.article.id = :articleId
          and c.deletedAt is null
          and (:afterDate is null or
               (:isAsc = true  and c.createdAt > :afterDate) or
               (:isAsc = false and c.createdAt < :afterDate))
          and (:cursor is null or
               (:isAsc = true and (
                   c.likeCount > :cursor or
                   (c.likeCount = :cursor and c.createdAt > :cursorDate)
               )) or
               (:isAsc = false and (
                   c.likeCount < :cursor or
                   (c.likeCount = :cursor and c.createdAt < :cursorDate)
               )))
""")
    Slice<Comment> findByArticle_IdWithLikeCountCursor(
            @Param("articleId") UUID articleId,
            @Param("cursor") Long cursor,
            @Param("cursorDate") LocalDateTime cursorDate,
            @Param("afterDate") LocalDateTime afterDate,
            @Param("isAsc") boolean isAsc,
            Pageable pageable
    );

    // 전체 댓글 수 조회 (삭제되지 않은 것만)
    @Query("""
           select count(c) from Comment c
           where c.article.id = :articleId
             and c.deletedAt is null
           """)
    long countByArticle_IdAndNotDeleted(@Param("articleId") UUID articleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Comment comment
              set comment.deletedAt = CURRENT_TIMESTAMP,
                  comment.updatedAt = CURRENT_TIMESTAMP
             where comment.id = :id and comment.deletedAt is null
           """)
    int softDeleteById(@Param("id") UUID id);

    long countByArticle_Id(UUID articleId);
}
