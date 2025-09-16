package com.sprint.team2.monew.global.log;

import com.sprint.team2.monew.global.config.aws.S3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

@RequiredArgsConstructor
@Component
@Slf4j
public class S3Uploader {
    private final S3Properties s3Properties;
    private final S3Client s3Client;

    public void uploadFile(Path path, String prefix) {
        log.info("[로그 관리] S3 저장 파일 이름 = {}", path.getFileName().toString());
        String key = prefix+"/"+path.getFileName().toString();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(s3Properties.bucket())
                        .key(key)
                        .contentType("text/plain")
                        .build();
        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromFile(path.toFile()));
        log.info("[로그 관리] S3 저장 실행 완료");
        // ⚠ 주입받은 s3Client는 닫지 말 것 (Bean 공용)
    }
}
