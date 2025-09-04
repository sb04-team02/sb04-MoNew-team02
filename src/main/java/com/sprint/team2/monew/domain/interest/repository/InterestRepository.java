package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> {
}
