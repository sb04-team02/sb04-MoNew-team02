package com.sprint.team2.monew.global.log;

import com.sprint.team2.monew.global.config.aws.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

@RequiredArgsConstructor
@Component
public class S3Uploader {
    private final S3Properties s3Properties;
    private final S3Client s3Client;

    public void uploadFile(Path path, String prefix) {
        String key = prefix+"/"+path.getFileName().toString();

        s3Client.putObject(req -> req
                .bucket(s3Properties.bucket())
                .key(key),
                RequestBody.fromFile(path.toFile()));

        // ⚠ 주입받은 s3Client는 닫지 말 것 (Bean 공용)
    }
}
