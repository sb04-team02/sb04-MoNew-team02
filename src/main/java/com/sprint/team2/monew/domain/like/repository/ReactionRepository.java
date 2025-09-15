package com.sprint.team2.monew.domain.like.repository;

import com.sprint.team2.monew.domain.like.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    //중복 체크를 위한 구문
    boolean existsByUser_IdAndComment_Id(UUID userId, UUID commentId);

    Optional<Reaction> findByUser_IdAndComment_Id(UUID userId, UUID commentId);

    @Modifying
    int deleteByUser_IdAndComment_Id(UUID userId, UUID commentId);

    void deleteByComment_Id(UUID commentId);
}
