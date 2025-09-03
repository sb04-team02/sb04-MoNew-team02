package com.sprint.team2.monew.domain.like.repository;

import com.sprint.team2.monew.domain.like.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
}
