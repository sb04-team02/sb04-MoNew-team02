package com.sprint.team2.monew.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "com.sprint.team2.monew.domain.userActivity.repository"
)
public class MongoConfig {
}