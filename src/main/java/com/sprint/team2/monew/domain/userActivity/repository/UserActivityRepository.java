package com.sprint.team2.monew.domain.userActivity.repository;

import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserActivityRepository extends MongoRepository<UserActivity, UUID> {

}
