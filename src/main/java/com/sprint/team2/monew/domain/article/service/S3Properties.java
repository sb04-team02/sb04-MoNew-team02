package com.sprint.team2.monew.domain.article.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "monew.backup.s3")
public record S3Properties(
    String accessKey,
    String secretKey,
    String region,
    String bucket,
    long presignedUrl
) {
}
