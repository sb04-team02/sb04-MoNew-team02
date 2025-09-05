package com.sprint.team2.monew.domain.notification.entity;

import com.sprint.team2.monew.domain.base.UpdatableEntity;
import com.sprint.team2.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notification extends UpdatableEntity {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 50, nullable = false)
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "confirmed")
    private boolean confirmed = false;

}
