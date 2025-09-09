package com.sprint.team2.monew.domain.interest.repository;

import com.sprint.team2.monew.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID> , InterestRepositoryCustom {
    @Query(nativeQuery = true,
            value = "SELECT EXISTS(" +
                    "SELECT 1 " +
                    "FROM interests " +
                    "WHERE bigm_similarity(name, :name) > 0.8);")
    boolean existsBySimilarityNameGreaterThan80Percent(@Param("name") String name);
}
