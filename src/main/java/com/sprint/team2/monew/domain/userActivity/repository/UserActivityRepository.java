package com.sprint.team2.monew.domain.userActivity.repository;

import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;


public interface UserActivityRepository extends MongoRepository<UserActivity, UUID> {

  List<UserActivity> findBySubscriptionsInterestId(UUID interestId);

}
