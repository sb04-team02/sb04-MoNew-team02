package com.sprint.team2.monew.global.log;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class FileWatchService {

    private WatchKey watchKey;
    private final S3Uploader s3Uploader;

    @PostConstruct
    public void fileMonitoring() throws IOException {

        String dirPath = ".logs"; // 모니터링 디렉토리

        // watchService 생성
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            log.info("watch service started!!!");

            // 경로 생성
            Path path = Paths.get(dirPath);

            // 해당 디렉토리 경로에 watchService와 이벤트 등록
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE, // 파일 생성
                    StandardWatchEventKinds.ENTRY_MODIFY, // 파일 수정
                    StandardWatchEventKinds.ENTRY_DELETE); // 파일 삭제

            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        try {
                            watchKey = watchService.take(); // 이벤트가 발생하면 watchKey 객체 반환 (이벤트 발생 전까지 블로킹 상태)
                        } catch (InterruptedException e) {
                            log.error("WatchService interrupted", e);
                            return; // 스레드를 종료시킴
                        }

                        List<WatchEvent<?>> watchEvents = watchKey.pollEvents(); // 이벤트 목록 가져옴

                        for (WatchEvent<?> watchEvent : watchEvents) { // 감지된 모든 이벤트 순회
                            // 이벤트 종류
                            WatchEvent.Kind<?> kind = watchEvent.kind();

                            // 경로
                            Path paths = (Path) watchEvent.context();

                            Path dir = Paths.get(dirPath);

                            if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                                Path changedPath = dir.resolve((Path) watchEvent.context());
                                String filename = changedPath.getFileName().toString();
                                // System.out.println("Uploading " + changedPath + " to S3...");
                                if (filename.matches("myapp\\.\\d{2}-\\d{2}-\\d{2}\\.log")) {
                                    log.info("[S3업로드] filename : {}", filename);
                                    s3Uploader.uploadFile(changedPath, "/logs");
                                }
                            } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                                // TODO : 폴더 내 파일 삭제
                            } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                                // TODO : 폴더 내 파일 수정
                            } else if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                                // TODO : 오버플로우
                            }
                        }

                        if (!watchKey.reset()) {
                            log.warn("WatchKey could not be reset. Exiting...");
                            watchService.close();
                            break; // WatchService를 닫고 루프 종료
                        }
                    }
                } catch (IOException e) {
                    log.error("Error closing WatchService", e);
                }
            });

            thread.setDaemon(true); // 메인 스레드가 종료되면 이 스레드도 종료됨
            thread.start();

        } catch (IOException e) {
            log.error("Failed to initialize WatchService", e);
        }
    }
}