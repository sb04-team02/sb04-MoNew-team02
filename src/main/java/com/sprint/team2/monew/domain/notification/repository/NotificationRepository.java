package com.sprint.team2.monew.domain.notification.repository;

import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Slice<Notification> findAllByUserIdAndConfirmedFalseOrderByCreatedAtDesc(
            UUID userId,
            LocalDateTime nextAfter,
            Pageable pageable);

    List<Notification> findAllByUserIdAndConfirmedIsFalse(UUID userId);

    List<Notification> findAllByConfirmedIsTrueAndUpdatedAtBefore(LocalDateTime threshold);

    Long countByUserId(UUID userId);

    List<Notification> findAllByUserId(UUID userId);

    Optional<Notification> findByUserId(UUID userId);

    void deleteByResourceTypeAndResourceId(ResourceType resourceType, UUID resourceId);
}
