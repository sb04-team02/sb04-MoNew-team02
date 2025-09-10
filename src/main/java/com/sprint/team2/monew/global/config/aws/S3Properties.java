package com.sprint.team2.monew.global.config.aws;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "monew.backup.s3")
public record S3Properties(
    String accessKey,
    String secretKey,
    String region,
    String bucket,
    long presignedUrl
) {
}
