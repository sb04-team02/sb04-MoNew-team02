package com.sprint.team2.monew.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(
        basePackages = "com.sprint.team2.monew.domain.userActivity.repository"
)
public class MongoConfig {
}