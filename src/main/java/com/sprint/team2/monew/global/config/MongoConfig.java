package com.sprint.team2.monew.global.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing(dateTimeProviderRef = "kstDateTimeProvider")
@EnableMongoRepositories(
        basePackages = "com.sprint.team2.monew.domain.userActivity.repository"
)
public class MongoConfig {

  @Bean(name = "kstDateTimeProvider")
  public DateTimeProvider kstDateTimeProvider() {
    return () -> Optional.of(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
  }

}