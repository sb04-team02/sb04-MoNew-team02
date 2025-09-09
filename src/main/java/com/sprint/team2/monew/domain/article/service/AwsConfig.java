package com.sprint.team2.monew.domain.article.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class AwsConfig {

  /**
   * S3Client 생성
   */
  @Bean
  public S3Client s3Client(S3Properties s3Properties) {
    return S3Client.builder()
        .region(Region.of(s3Properties.region()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.accessKey(), s3Properties.secretKey())
            )
        )
        .build();
  }
}
