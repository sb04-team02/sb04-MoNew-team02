package com.sprint.team2.monew.global.config.aws;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
@Profile("test")
public class DummyAwsConfig {

    @Bean
    public S3Client s3Client() {
        // 더미 객체 주입
        return org.mockito.Mockito.mock(S3Client.class);
    }
}
