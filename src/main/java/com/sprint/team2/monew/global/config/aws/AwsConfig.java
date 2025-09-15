package com.sprint.team2.monew.global.config.aws;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class AwsConfig {

  /**
   * S3Client 생성
   * SDK가 환경 변수(AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)에서
   * 지역과 자격 증명을 자동으로 찾아 구성
   */
  @Bean
  public S3Client s3Client(S3Properties s3Properties) {
//    return S3Client.builder()
//        .region(Region.of(s3Properties.region()))
//        .credentialsProvider(
//            StaticCredentialsProvider.create(
//                AwsBasicCredentials.create(s3Properties.accessKey(), s3Properties.secretKey())
//            )
//        )
//        .build();
    return S3Client.builder().build();
  }
}
