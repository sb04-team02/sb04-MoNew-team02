package com.sprint.team2.monew.domain.notification.repository;

import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
        SELECT n FROM Notification n 
        WHERE n.user.id = :userId
          AND n.confirmed = false
          AND n.createdAt < COALESCE(:nextAfter, CURRENT_TIMESTAMP ) 
        ORDER BY n.createdAt DESC
    """)
    Slice<Notification> findAllByUserIdWithCursorPaging(
            @Param("userId") UUID userId,
            @Param("nextAfter") LocalDateTime nextAfter,
            Pageable pageable);

    List<Notification> findAllByUserIdAndConfirmedIsFalse(UUID userId);

    List<Notification> findAllByConfirmedIsTrueAndUpdatedAtBefore(LocalDateTime threshold);

    Long countByUserIdAndConfirmedFalse(UUID userId);

    void deleteByResourceTypeAndResourceId(ResourceType resourceType, UUID resourceId);
}
