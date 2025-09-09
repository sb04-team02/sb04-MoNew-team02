package com.sprint.team2.monew.global.config;

import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = "com.sprint.team2.monew.domain",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { UserActivityRepository.class }
        )
)
public class JpaAuditingConfig {
}
