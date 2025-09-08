package com.sprint.team2.monew.domain.user.repository;

import com.sprint.team2.monew.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.deletedAt <= :threshold")
    List<User> findAllByDeletedAtBefore(@Param("threshold") LocalDateTime threshold);
}
