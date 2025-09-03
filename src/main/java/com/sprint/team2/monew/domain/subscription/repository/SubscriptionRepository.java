package com.sprint.team2.monew.domain.subscription.repository;

import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
}
