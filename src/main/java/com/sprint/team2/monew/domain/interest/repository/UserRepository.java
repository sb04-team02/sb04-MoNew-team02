package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.subscription.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
